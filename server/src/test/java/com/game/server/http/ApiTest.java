package com.game.server.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.server.core.Matchmaker;
import com.game.server.metrics.ServiceMetrics;
import com.game.server.store.InMemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The real server on an ephemeral port, driven over HTTP. Time is simulated, ticks are manual. */
class ApiTest {

    private final AtomicLong now = new AtomicLong(1_000_000);
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private Matchmaker matchmaker;
    private Api api;
    private String base;

    record Reply(int status, JsonNode body, String raw) {}

    @BeforeEach
    void start() throws Exception {
        final InMemoryStore store = new InMemoryStore();
        final ServiceMetrics metrics = new ServiceMetrics();
        matchmaker = new Matchmaker(Matchmaker.Policy.defaults(), now::get, store::saveMatches, metrics);
        metrics.watch(matchmaker);
        api = new Api(0, matchmaker, store, metrics, now::get, 5_000);
        api.start();
        base = "http://localhost:" + api.port();
    }

    @AfterEach
    void stop() {
        api.close();
    }

    private Reply call(String method, String path, String body) throws Exception {
        final HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path));
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            b.header("Content-Type", "application/json");
        }
        final HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        final String raw = r.body();
        JsonNode node = null;
        if (raw != null && raw.startsWith("{")) {
            node = json.readTree(raw);
        }
        return new Reply(r.statusCode(), node, raw);
    }

    private String join(String player, String game, int hint) throws Exception {
        final Reply r = call("POST", "/v1/tickets",
                "{\"playerId\":\"" + player + "\",\"game\":\"" + game + "\",\"ratingHint\":" + hint + "}");
        assertEquals(201, r.status(), r.raw());
        return r.body().get("ticketId").asText();
    }

    @Test
    void fullMatchFlowUpdatesRatingsAndLeaderboard() throws Exception {
        final String ta = join("alice", "checkers", 1500);
        now.addAndGet(100);
        final String tb = join("bob", "Checkers", 1520);
        assertEquals("SEARCHING", call("GET", "/v1/tickets/" + ta, null).body().get("status").asText());

        now.addAndGet(1_500);
        matchmaker.tick();
        final JsonNode a = call("GET", "/v1/tickets/" + ta, null).body();
        final JsonNode b = call("GET", "/v1/tickets/" + tb, null).body();
        assertEquals("MATCHED", a.get("status").asText());
        assertEquals(a.get("matchId").asText(), b.get("matchId").asText());
        assertEquals("bob", a.get("opponentId").asText());
        assertEquals(1500, b.get("opponentRating").asInt());
        assertEquals(1_600, a.get("waitedMillis").asLong());
        assertEquals(1_500, b.get("waitedMillis").asLong());

        final String matchId = a.get("matchId").asText();
        assertEquals("alice", call("GET", "/v1/matches/" + matchId, null).body().get("playerA").asText());

        final Reply result = call("POST", "/v1/matches/" + matchId + "/result",
                "{\"reportedBy\":\"bob\",\"winner\":\"alice\"}");
        assertEquals(200, result.status(), result.raw());
        assertTrue(result.body().get("deltaA").asInt() > 0);
        assertEquals(409, call("POST", "/v1/matches/" + matchId + "/result",
                "{\"reportedBy\":\"alice\",\"winner\":\"alice\"}").status());

        final JsonNode board = call("GET", "/v1/leaderboard/checkers?limit=5", null).body();
        assertEquals("alice", board.get("players").get(0).get("playerId").asText());
        final JsonNode profile = call("GET", "/v1/players/alice", null).body();
        assertEquals(1, profile.get("ratings").get(0).get("wins").asInt());
    }

    @Test
    void placementHintOnlyAppliesToNewPlayersAndIsClamped() throws Exception {
        final String t = join("carol", "connect4", 9999);
        assertEquals(Api.MAX_HINT, call("GET", "/v1/tickets/" + t, null).body().get("rating").asInt());
        assertEquals(204, call("DELETE", "/v1/tickets/" + t, null).status());
        final String again = join("carol", "connect4", 900);
        assertEquals(Api.MAX_HINT, call("GET", "/v1/tickets/" + again, null).body().get("rating").asInt());
    }

    @Test
    void validationAndErrorCodes() throws Exception {
        assertEquals(400, call("POST", "/v1/tickets", "{\"playerId\":\"x\",\"game\":\"chess\"}").status());
        assertEquals(400, call("POST", "/v1/tickets", "{\"playerId\":\"has space\",\"game\":\"checkers\"}").status());
        assertEquals(400, call("POST", "/v1/tickets", "not json").status());
        assertEquals(400, call("POST", "/v1/tickets", "x".repeat(20_000)).status());
        join("dave", "tictactoe", 1200);
        final Reply dup = call("POST", "/v1/tickets", "{\"playerId\":\"dave\",\"game\":\"checkers\"}");
        assertEquals(409, dup.status());
        assertEquals("already_searching", dup.body().get("error").asText());
        assertEquals(404, call("GET", "/v1/tickets/nope", null).status());
        assertEquals(404, call("DELETE", "/v1/tickets/nope", null).status());
        assertEquals(404, call("GET", "/v1/players/nobody", null).status());
        assertEquals(404, call("GET", "/v2/anything", null).status());
        assertEquals(405, call("PUT", "/v1/tickets", "{}").status());
        assertEquals(400, call("GET", "/v1/leaderboard/checkers?limit=1000", null).status());
        assertEquals(404, call("POST", "/v1/matches/" + java.util.UUID.randomUUID() + "/result",
                "{\"reportedBy\":\"dave\",\"winner\":null}").status());
    }

    @Test
    void healthReflectsTheMatchmakerAndDraining() throws Exception {
        matchmaker.tick();
        assertEquals(200, call("GET", "/health/live", null).status());
        assertEquals(200, call("GET", "/health/ready", null).status());
        now.addAndGet(6_000);  // no tick for 6 s: the matchmaker looks stuck
        assertEquals(503, call("GET", "/health/live", null).status());
        matchmaker.tick();
        api.drain();
        assertEquals(200, call("GET", "/health/live", null).status());
        assertEquals("draining", call("GET", "/health/ready", null).body().get("status").asText());
    }

    @Test
    void metricsEndpointExposesTrafficAndMatchmakingSeries() throws Exception {
        join("erin", "checkers", 1500);
        join("frank", "checkers", 1500);
        now.addAndGet(2_000);
        matchmaker.tick();
        call("GET", "/v1/tickets/nope", null);
        final Reply m = call("GET", "/metrics", null);
        assertEquals(200, m.status());
        final List<String> expected = new ArrayList<>(List.of(
                "omg_tickets_created_total{game=\"checkers\"} 2",
                "omg_matches_total{game=\"checkers\"} 1",
                "omg_time_to_match_seconds_count{game=\"checkers\"} 2",
                "omg_time_to_match_seconds_bucket{game=\"checkers\",le=\"2\"} 2",
                "omg_queue_depth{game=\"checkers\"} 0",
                "omg_http_requests_total{route=\"/v1/tickets\",method=\"POST\",status=\"201\"} 2",
                "omg_http_requests_total{route=\"/v1/tickets/{id}\",method=\"GET\",status=\"404\"} 1",
                "# TYPE omg_http_request_duration_seconds histogram",
                "jvm_memory_used_bytes{area=\"heap\"}"));
        for (String line : expected) {
            assertTrue(m.raw().contains(line), "missing: " + line + "\n" + m.raw());
        }
    }

    @Test
    void handlesManyConcurrentPlayers() throws Exception {
        final int players = 400;
        final List<Thread> threads = new ArrayList<>();
        final List<String> tickets = java.util.Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < players; i++) {
            final int n = i;
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    tickets.add(join("p" + n, "connect4", 1400 + (n % 200)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(players, tickets.size());
        now.addAndGet(3_000);
        assertEquals(players / 2, matchmaker.tick().size());
    }
}
