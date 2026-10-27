package com.game.server.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

/**
 * Elo-based matchmaking with a search window that widens the longer a player waits.
 *
 * <p>Every tick, per game, tickets are visited oldest first. Each one is paired with the closest-rated
 * ticket still unpaired, if the rating gap fits inside the older ticket's window
 * ({@code base + widenPerSecond * secondsWaited}, capped). Players who just joined only get close
 * opponents; players who have waited a while accept wider gaps, so nobody waits forever. Finding the
 * closest rating uses a sorted index, so a tick is O(n log n) in the queue size.
 *
 * <p>Pairs are written to the {@link MatchSink} (the database) <em>before</em> either player is told
 * about the match. If that write fails, both tickets go back into the queue: a client is never sent
 * a match id that the result endpoint wouldn't recognise.
 *
 * <p>The clock is injected, so tests and the simulator drive time explicitly and get the same answer
 * every run.
 */
public final class Matchmaker {

    /** Tuning. The defaults keep the original team design: start at ±50, widen by 25 per second, cap at 400, give up after 60 s. */
    public record Policy(int baseWindow, int widenPerSecond, int maxWindow, long ticketTimeoutMillis,
                         long retainResolvedMillis) {
        public static Policy defaults() {
            return new Policy(50, 25, 400, 60_000, 120_000);
        }

        public int window(long waitedMillis) {
            final long widened = baseWindow + widenPerSecond * waitedMillis / 1000;
            return (int) Math.min(maxWindow, widened);
        }
    }

    /** Where matches are persisted before players hear about them. */
    @FunctionalInterface
    public interface MatchSink {
        void persist(List<Match> matches) throws Exception;
    }

    /** Hooks for metrics and logs. All callbacks run outside the matchmaker's lock. */
    public interface Events {
        default void ticketCreated(Game game) {}
        default void matched(Match match) {}
        default void expired(Game game, long waitedMillis) {}
        default void cancelled(Game game) {}
        default void tick(long durationNanos, int matches) {}
        default void persistFailed(int matches, Exception error) {}
    }

    public static final class DuplicateTicketException extends RuntimeException {
        private final String existingTicketId;

        DuplicateTicketException(String playerId, String existingTicketId) {
            super("player " + playerId + " is already searching (ticket " + existingTicketId + ")");
            this.existingTicketId = existingTicketId;
        }

        public String existingTicketId() {
            return existingTicketId;
        }
    }

    public enum CancelOutcome { CANCELLED, NOT_FOUND, ALREADY_RESOLVED }

    private final Policy policy;
    private final LongSupplier clock;
    private final MatchSink sink;
    private final Events events;
    private final ReentrantLock lock = new ReentrantLock();

    // All guarded by lock.
    private final Map<String, Ticket> tickets = new HashMap<>();            // every ticket we still remember, by id
    private final Map<String, Ticket> activeByPlayer = new HashMap<>();     // SEARCHING or being paired
    private final Map<Game, Map<String, Ticket>> pools = new EnumMap<>(Game.class);  // SEARCHING, by ticket id
    private final Map<String, Ticket> pairing = new HashMap<>();            // picked this tick, not yet persisted
    private final ArrayDeque<Ticket> resolved = new ArrayDeque<>();          // in resolution order, for eviction

    private long nextSequence;  // guarded by lock
    private volatile long lastTickMillis;

    public Matchmaker(Policy policy, LongSupplier clock, MatchSink sink, Events events) {
        this.policy = policy;
        this.clock = clock;
        this.sink = sink;
        this.events = events;
        for (Game g : Game.values()) {
            pools.put(g, new HashMap<>());
        }
        this.lastTickMillis = clock.getAsLong();
    }

    public Policy policy() {
        return policy;
    }

    public Ticket.View submit(String playerId, Game game, int rating) {
        final long now = clock.getAsLong();
        final Ticket ticket;
        lock.lock();
        try {
            final Ticket existing = activeByPlayer.get(playerId);
            if (existing != null) {
                throw new DuplicateTicketException(playerId, existing.id());
            }
            ticket = new Ticket(UUID.randomUUID().toString(), playerId, game, rating, now, nextSequence++);
            tickets.put(ticket.id(), ticket);
            activeByPlayer.put(playerId, ticket);
            pools.get(game).put(ticket.id(), ticket);
        } finally {
            lock.unlock();
        }
        events.ticketCreated(game);
        return ticket.view(now);
    }

    public Optional<Ticket.View> get(String ticketId) {
        final long now = clock.getAsLong();
        lock.lock();
        try {
            final Ticket t = tickets.get(ticketId);
            return Optional.ofNullable(t).map(x -> x.view(now));
        } finally {
            lock.unlock();
        }
    }

    public CancelOutcome cancel(String ticketId) {
        final long now = clock.getAsLong();
        final Game game;
        lock.lock();
        try {
            final Ticket t = tickets.get(ticketId);
            if (t == null) {
                return CancelOutcome.NOT_FOUND;
            }
            if (t.status() != Ticket.Status.SEARCHING || pairing.containsKey(t.id())) {
                return CancelOutcome.ALREADY_RESOLVED;
            }
            pools.get(t.game()).remove(t.id());
            activeByPlayer.remove(t.playerId());
            t.close(Ticket.Status.CANCELLED, now);
            resolved.addLast(t);
            game = t.game();
        } finally {
            lock.unlock();
        }
        events.cancelled(game);
        return CancelOutcome.CANCELLED;
    }

    public int queueDepth(Game game) {
        lock.lock();
        try {
            return pools.get(game).size();
        } finally {
            lock.unlock();
        }
    }

    public long lastTickMillis() {
        return lastTickMillis;
    }

    /** One matchmaking pass over every game. Returns the matches made. */
    public List<Match> tick() {
        final long started = System.nanoTime();
        final long now = clock.getAsLong();
        final List<Match> matches = new ArrayList<>();
        final List<Ticket> expired = new ArrayList<>();

        lock.lock();
        try {
            evictResolved(now);
            for (Game game : Game.values()) {
                pair(game, now, matches, expired);
            }
        } finally {
            lock.unlock();
        }
        for (Ticket t : expired) {
            events.expired(t.game(), now - t.createdAtMillis());
        }

        if (!matches.isEmpty()) {
            Exception failure = null;
            try {
                sink.persist(matches);
            } catch (Exception e) {
                failure = e;
            }
            commit(matches, failure == null, clock.getAsLong());
            if (failure != null) {
                events.persistFailed(matches.size(), failure);
                matches.clear();
            } else {
                for (Match m : matches) {
                    events.matched(m);
                }
            }
        }
        lastTickMillis = now;
        events.tick(System.nanoTime() - started, matches.size());
        return matches;
    }

    // ---------------------------------------------------------------------------------------------

    private void pair(Game game, long now, List<Match> matches, List<Ticket> expired) {
        final Map<String, Ticket> pool = pools.get(game);
        if (pool.isEmpty()) {
            return;
        }
        final List<Ticket> byAge = new ArrayList<>(pool.values());
        byAge.sort(Comparator.comparingLong(Ticket::createdAtMillis).thenComparingLong(Ticket::sequence));

        // Expire first, then index the rest by rating.
        final TreeMap<Integer, ArrayDeque<Ticket>> byRating = new TreeMap<>();
        final List<Ticket> live = new ArrayList<>(byAge.size());
        for (Ticket t : byAge) {
            if (now - t.createdAtMillis() >= policy.ticketTimeoutMillis()) {
                pool.remove(t.id());
                activeByPlayer.remove(t.playerId());
                t.close(Ticket.Status.EXPIRED, now);
                resolved.addLast(t);
                expired.add(t);
            } else {
                byRating.computeIfAbsent(t.rating(), r -> new ArrayDeque<>()).addLast(t);
                live.add(t);
            }
        }

        for (Ticket t : live) {
            if (!remove(byRating, t)) {
                continue;  // already paired with an older ticket this tick
            }
            final Ticket candidate = closest(byRating, t.rating());
            if (candidate == null) {
                break;  // nobody left
            }
            final long waited = now - t.createdAtMillis();
            if (Math.abs(candidate.rating() - t.rating()) > policy.window(waited)) {
                // t is the oldest ticket left, so its window is the widest: nobody can take t this tick.
                continue;
            }
            remove(byRating, candidate);
            pool.remove(t.id());
            pool.remove(candidate.id());
            pairing.put(t.id(), t);
            pairing.put(candidate.id(), candidate);
            matches.add(new Match(UUID.randomUUID().toString(), game, t.playerId(), t.rating(),
                    candidate.playerId(), candidate.rating(), now, waited, now - candidate.createdAtMillis()));
        }
    }

    private void commit(List<Match> matches, boolean persisted, long now) {
        lock.lock();
        try {
            for (Match m : matches) {
                final Ticket a = activeByPlayer.get(m.playerA());
                final Ticket b = activeByPlayer.get(m.playerB());
                pairing.remove(a.id());
                pairing.remove(b.id());
                if (persisted) {
                    a.matched(m.id(), b, now);
                    b.matched(m.id(), a, now);
                    activeByPlayer.remove(a.playerId());
                    activeByPlayer.remove(b.playerId());
                    resolved.addLast(a);
                    resolved.addLast(b);
                } else {
                    // Back in the queue with their original join time: no one loses their place.
                    pools.get(m.game()).put(a.id(), a);
                    pools.get(m.game()).put(b.id(), b);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void evictResolved(long now) {
        while (!resolved.isEmpty() && now - resolved.peekFirst().resolvedAtMillis() > policy.retainResolvedMillis()) {
            tickets.remove(resolved.pollFirst().id());
        }
    }

    private static boolean remove(TreeMap<Integer, ArrayDeque<Ticket>> index, Ticket t) {
        final ArrayDeque<Ticket> bucket = index.get(t.rating());
        if (bucket == null || !bucket.remove(t)) {
            return false;
        }
        if (bucket.isEmpty()) {
            index.remove(t.rating());
        }
        return true;
    }

    /** Closest rating in the index; on a tie between above and below, the one who has waited longer. */
    private static Ticket closest(TreeMap<Integer, ArrayDeque<Ticket>> index, int rating) {
        final Map.Entry<Integer, ArrayDeque<Ticket>> lo = index.floorEntry(rating);
        final Map.Entry<Integer, ArrayDeque<Ticket>> hi = index.ceilingEntry(rating);
        if (lo == null && hi == null) {
            return null;
        }
        if (lo == null) {
            return hi.getValue().peekFirst();
        }
        if (hi == null) {
            return lo.getValue().peekFirst();
        }
        final int dLo = rating - lo.getKey();
        final int dHi = hi.getKey() - rating;
        if (dLo != dHi) {
            return dLo < dHi ? lo.getValue().peekFirst() : hi.getValue().peekFirst();
        }
        final Ticket a = lo.getValue().peekFirst();
        final Ticket b = hi.getValue().peekFirst();
        return a.sequence() < b.sequence() ? a : b;
    }
}
