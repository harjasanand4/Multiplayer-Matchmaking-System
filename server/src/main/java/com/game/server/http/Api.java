package com.game.server.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.server.core.Game;
import com.game.server.core.Matchmaker;
import com.game.server.core.Ticket;
import com.game.server.metrics.ServiceMetrics;
import com.game.server.store.JdbcStore;
import com.game.server.store.Store;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The HTTP API, on the JDK's built-in server with one virtual thread per request.
 *
 * <pre>
 *   POST   /v1/tickets                  {"playerId", "game", "ratingHint"?}  join the queue
 *   GET    /v1/tickets/{id}                                                  poll: SEARCHING / MATCHED / EXPIRED / CANCELLED
 *   DELETE /v1/tickets/{id}                                                  leave the queue
 *   GET    /v1/matches/{id}
 *   POST   /v1/matches/{id}/result      {"reportedBy", "winner"|null}       report once; updates both ratings
 *   GET    /v1/players/{id}                                                  ratings per game
 *   GET    /v1/leaderboard/{game}?limit=10
 *   GET    /health/live  /health/ready  /metrics
 * </pre>
 */
public final class Api implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(Api.class.getName());

    static {
        // Two JDK HttpServer defaults that the load test tripped over:
        //  - Nagle's algorithm is on, so a response written as headers then body could sit ~40 ms
        //    waiting for the client's delayed ACK.
        //  - Only 200 idle keep-alive connections are kept; beyond that the server closes a connection
        //    right after responding, racing the client's next request on it. Clients retry GETs
        //    transparently but not POSTs, which then fail with EOF.
        setDefault("sun.net.httpserver.nodelay", "true");
        setDefault("sun.net.httpserver.maxIdleConnections", "4096");
    }

    private static void setDefault(String property, String value) {
        if (System.getProperty(property) == null) {
            System.setProperty(property, value);
        }
    }
    private static final int MAX_BODY_BYTES = 16 * 1024;
    private static final Pattern PLAYER_ID = Pattern.compile("[A-Za-z0-9_.:-]{1,64}");
    /** A first-time player's placement rating is clamped to this range. */
    static final int MIN_HINT = 800;
    static final int MAX_HINT = 2400;

    record TicketRequest(String playerId, String game, Integer ratingHint) {}

    record ResultRequest(String reportedBy, String winner) {}

    record Error(String error, String message) {}

    @FunctionalInterface
    private interface Handler {
        Response handle(HttpExchange ex, Matcher path) throws IOException;
    }

    private record Route(String method, Pattern path, String name, Handler handler) {}

    private record Response(int status, Object body, Map<String, String> headers) {
        static Response json(int status, Object body) {
            return new Response(status, body, Map.of());
        }
    }

    private final Matchmaker matchmaker;
    private final Store store;
    private final ServiceMetrics metrics;
    private final LongSupplier clock;
    private final long livenessStaleMillis;
    private final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    private final List<Route> routes = new ArrayList<>();
    private final HttpServer server;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean draining;

    public Api(int port, Matchmaker matchmaker, Store store, ServiceMetrics metrics, LongSupplier clock,
               long livenessStaleMillis) throws IOException {
        this.matchmaker = matchmaker;
        this.store = store;
        this.metrics = metrics;
        this.clock = clock;
        this.livenessStaleMillis = livenessStaleMillis;

        route("POST", "/v1/tickets", "/v1/tickets", (ex, m) -> createTicket(ex));
        route("GET", "/v1/tickets/([^/]+)", "/v1/tickets/{id}", (ex, m) -> getTicket(m.group(1)));
        route("DELETE", "/v1/tickets/([^/]+)", "/v1/tickets/{id}", (ex, m) -> cancelTicket(m.group(1)));
        route("GET", "/v1/matches/([^/]+)", "/v1/matches/{id}", (ex, m) -> getMatch(m.group(1)));
        route("POST", "/v1/matches/([^/]+)/result", "/v1/matches/{id}/result", (ex, m) -> report(ex, m.group(1)));
        route("GET", "/v1/players/([^/]+)", "/v1/players/{id}", (ex, m) -> player(m.group(1)));
        route("GET", "/v1/leaderboard/([^/]+)", "/v1/leaderboard/{game}", (ex, m) -> leaderboard(ex, m.group(1)));
        route("GET", "/health/live", "/health/live", (ex, m) -> live());
        route("GET", "/health/ready", "/health/ready", (ex, m) -> ready());
        route("GET", "/metrics", "/metrics", (ex, m) -> metricsPage());

        server = HttpServer.create(new InetSocketAddress(port), 1024);
        server.createContext("/", this::dispatch);
        server.setExecutor(executor);
    }

    public void start() {
        server.start();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    /** Fail readiness so load balancers stop sending traffic; requests in flight still complete. */
    public void drain() {
        draining = true;
    }

    @Override
    public void close() {
        server.stop(1);
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void route(String method, String regex, String name, Handler handler) {
        routes.add(new Route(method, Pattern.compile(regex), name, handler));
    }

    private void dispatch(HttpExchange ex) {
        final long started = System.nanoTime();
        String routeName = "unmatched";
        Response response;
        try {
            final String path = ex.getRequestURI().getPath();
            Route found = null;
            Matcher matcher = null;
            boolean pathKnown = false;
            for (Route r : routes) {
                final Matcher m = r.path().matcher(path);
                if (m.matches()) {
                    pathKnown = true;
                    if (r.method().equals(ex.getRequestMethod())) {
                        found = r;
                        matcher = m;
                        break;
                    }
                }
            }
            if (found != null) {
                routeName = found.name();
                response = found.handler().handle(ex, matcher);
            } else if (pathKnown) {
                response = error(405, "method_not_allowed", ex.getRequestMethod() + " is not supported here");
            } else {
                response = error(404, "not_found", "no route for " + path);
            }
        } catch (BadRequest e) {
            response = error(400, "bad_request", e.getMessage());
        } catch (JdbcStore.StoreException e) {
            metrics.storeError(routeName);
            LOG.log(Level.WARNING, "database error on " + routeName + ": " + e.getMessage());
            response = error(503, "unavailable", "database unavailable, try again");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "unhandled error on " + routeName, e);
            response = error(500, "internal", "internal error");
        }
        final int status = send(ex, response);
        final long elapsed = System.nanoTime() - started;
        metrics.request(routeName, ex.getRequestMethod(), status, elapsed);
        if (status >= 500 || elapsed > 250_000_000L) {
            LOG.warning(String.format("%s %s -> %d in %.1f ms", ex.getRequestMethod(), ex.getRequestURI().getPath(),
                    status, elapsed / 1e6));
        }
    }

    private int send(HttpExchange ex, Response r) {
        try (ex) {
            final byte[] body;
            if (r.body() instanceof String s) {
                body = s.getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            } else if (r.body() == null) {
                body = new byte[0];
            } else {
                body = json.writeValueAsBytes(r.body());
                ex.getResponseHeaders().set("Content-Type", "application/json");
            }
            r.headers().forEach((k, v) -> ex.getResponseHeaders().set(k, v));
            ex.sendResponseHeaders(r.status(), body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                try (OutputStream out = ex.getResponseBody()) {
                    out.write(body);
                }
            }
        } catch (IOException e) {
            // Client went away mid-response; nothing useful to do.
        }
        return r.status();
    }

    private <T> T body(HttpExchange ex, Class<T> type) throws IOException {
        final byte[] raw;
        try (InputStream in = ex.getRequestBody()) {
            raw = in.readNBytes(MAX_BODY_BYTES + 1);
        }
        if (raw.length > MAX_BODY_BYTES) {
            throw new BadRequest("request body larger than " + MAX_BODY_BYTES + " bytes");
        }
        try {
            final T value = json.readValue(raw, type);
            if (value == null) {
                throw new BadRequest("empty body");
            }
            return value;
        } catch (JsonProcessingException e) {
            throw new BadRequest("invalid JSON: " + e.getOriginalMessage());
        }
    }

    private static final class BadRequest extends RuntimeException {
        BadRequest(String message) {
            super(message);
        }
    }

    private static Response error(int status, String code, String message) {
        return Response.json(status, new Error(code, message));
    }

    private static String playerId(String raw, String field) {
        if (raw == null || !PLAYER_ID.matcher(raw).matches()) {
            throw new BadRequest(field + " must be 1-64 characters of letters, digits, '_', '.', ':' or '-'");
        }
        return raw;
    }

    private static Game game(String raw) {
        return Game.parse(raw).orElseThrow(() -> new BadRequest("game must be one of tictactoe, checkers, connect4"));
    }

    // ---------------------------------------------------------------------------------------------

    private Response createTicket(HttpExchange ex) throws IOException {
        final TicketRequest req = body(ex, TicketRequest.class);
        final String playerId = playerId(req.playerId(), "playerId");
        final Game game = game(req.game());
        final int hint = req.ratingHint() == null ? com.game.server.core.Elo.DEFAULT_RATING
                : Math.max(MIN_HINT, Math.min(MAX_HINT, req.ratingHint()));
        // The rating comes from the database; the hint only seeds a brand-new player.
        final int rating = store.ensurePlayer(playerId, game, hint).rating();
        try {
            final Ticket.View t = matchmaker.submit(playerId, game, rating);
            return new Response(201, t, Map.of("Location", "/v1/tickets/" + t.ticketId()));
        } catch (Matchmaker.DuplicateTicketException e) {
            final Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", "already_searching");
            body.put("message", e.getMessage());
            body.put("ticketId", e.existingTicketId());
            return Response.json(409, body);
        }
    }

    private Response getTicket(String id) {
        return matchmaker.get(id).map(t -> Response.json(200, t))
                .orElseGet(() -> error(404, "not_found", "no such ticket (it may have expired from memory)"));
    }

    private Response cancelTicket(String id) {
        return switch (matchmaker.cancel(id)) {
            case CANCELLED -> new Response(204, null, Map.of());
            case NOT_FOUND -> error(404, "not_found", "no such ticket");
            case ALREADY_RESOLVED -> error(409, "already_resolved", "ticket is already matched, expired or cancelled");
        };
    }

    private Response getMatch(String id) {
        return store.match(id).map(m -> Response.json(200, m))
                .orElseGet(() -> error(404, "not_found", "no such match"));
    }

    private Response report(HttpExchange ex, String matchId) throws IOException {
        final ResultRequest req = body(ex, ResultRequest.class);
        final String reportedBy = playerId(req.reportedBy(), "reportedBy");
        final String winner = req.winner() == null ? null : playerId(req.winner(), "winner");
        final Store.Report r = store.reportResult(matchId, reportedBy, winner);
        if (r instanceof Store.Report.Applied applied) {
            metrics.result(winner == null ? "draw" : "win");
            return Response.json(200, applied);
        }
        if (r instanceof Store.Report.NotFound) {
            metrics.result("not_found");
            return error(404, "not_found", "no such match");
        }
        if (r instanceof Store.Report.AlreadyReported) {
            metrics.result("duplicate");
            return error(409, "already_reported", "a result for this match was already recorded");
        }
        metrics.result("rejected");
        return error(403, "not_a_participant", "reportedBy and winner must be players in this match");
    }

    private Response player(String id) {
        final Map<Game, Store.Rating> ratings = store.player(playerId(id, "player id"));
        if (ratings.isEmpty()) {
            return error(404, "not_found", "unknown player");
        }
        return Response.json(200, Map.of("playerId", id, "ratings", ratings.values()));
    }

    private Response leaderboard(HttpExchange ex, String gameName) {
        final Game game = game(gameName);
        int limit = 10;
        final Optional<String> q = Optional.ofNullable(URI.create(ex.getRequestURI().toString()).getQuery());
        if (q.isPresent()) {
            for (String part : q.get().split("&")) {
                if (part.startsWith("limit=")) {
                    try {
                        limit = Integer.parseInt(part.substring(6));
                    } catch (NumberFormatException e) {
                        throw new BadRequest("limit must be a number");
                    }
                }
            }
        }
        if (limit < 1 || limit > 100) {
            throw new BadRequest("limit must be between 1 and 100");
        }
        return Response.json(200, Map.of("game", game, "players", store.leaderboard(game, limit)));
    }

    private Response live() {
        final long sinceTick = clock.getAsLong() - matchmaker.lastTickMillis();
        if (sinceTick > livenessStaleMillis) {
            return Response.json(503, Map.of("status", "matchmaker stalled", "msSinceLastTick", sinceTick));
        }
        return Response.json(200, Map.of("status", "ok"));
    }

    private Response ready() {
        if (draining) {
            return Response.json(503, Map.of("status", "draining"));
        }
        if (live().status() != 200) {
            return Response.json(503, Map.of("status", "matchmaker stalled"));
        }
        if (!store.healthy()) {
            return Response.json(503, Map.of("status", "database unavailable"));
        }
        return Response.json(200, Map.of("status", "ready"));
    }

    private Response metricsPage() {
        return Response.json(200, metrics.registry().scrape());
    }
}
