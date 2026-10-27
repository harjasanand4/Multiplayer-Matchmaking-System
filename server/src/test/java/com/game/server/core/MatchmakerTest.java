package com.game.server.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchmakerTest {

    private long now;
    private final List<Match> persisted = new ArrayList<>();
    private final AtomicBoolean failPersist = new AtomicBoolean();
    private final List<Long> expiredWaits = new ArrayList<>();
    private Matchmaker mm;

    @BeforeEach
    void setUp() {
        now = 1_000_000;
        mm = new Matchmaker(Matchmaker.Policy.defaults(), () -> now, matches -> {
            if (failPersist.get()) {
                throw new IllegalStateException("database down");
            }
            persisted.addAll(matches);
        }, new Matchmaker.Events() {
            @Override
            public void expired(Game game, long waitedMillis) {
                expiredWaits.add(waitedMillis);
            }
        });
    }

    private Ticket.View status(Ticket.View t) {
        return mm.get(t.ticketId()).orElseThrow();
    }

    @Test
    void windowStartsAt50AndWidensBy25PerSecondUpTo400() {
        final Matchmaker.Policy p = Matchmaker.Policy.defaults();
        assertEquals(50, p.window(0));
        assertEquals(75, p.window(1_000));
        assertEquals(175, p.window(5_000));
        assertEquals(400, p.window(14_000));
        assertEquals(400, p.window(59_000));
    }

    @Test
    void closeRatingsMatchOnTheNextTick() {
        final Ticket.View a = mm.submit("alice", Game.CHECKERS, 1500);
        final Ticket.View b = mm.submit("bob", Game.CHECKERS, 1540);
        final List<Match> made = mm.tick();
        assertEquals(1, made.size());
        assertEquals(Ticket.Status.MATCHED, status(a).status());
        assertEquals(Ticket.Status.MATCHED, status(b).status());
        assertEquals(status(a).matchId(), status(b).matchId());
        assertEquals("bob", status(a).opponentId());
        assertEquals(1500, status(b).opponentRating());
        assertEquals(made, persisted);
        assertEquals(0, mm.queueDepth(Game.CHECKERS));
    }

    @Test
    void farApartRatingsWaitUntilTheWindowIsWideEnough() {
        final Ticket.View a = mm.submit("alice", Game.CONNECT4, 1500);
        now += 2_000;
        final Ticket.View b = mm.submit("bob", Game.CONNECT4, 1640);  // gap 140
        mm.tick();  // alice has waited 2 s: window 100
        assertEquals(Ticket.Status.SEARCHING, status(a).status());
        now += 1_000;
        mm.tick();  // 3 s: 125
        assertEquals(Ticket.Status.SEARCHING, status(b).status());
        now += 1_000;
        mm.tick();  // 4 s: 150 >= 140
        assertEquals(Ticket.Status.MATCHED, status(a).status());
        assertEquals(4_000, status(a).waitedMillis());
        assertEquals(2_000, status(b).waitedMillis());
    }

    @Test
    void picksTheClosestOpponentNotTheFirstOneInTheQueue() {
        mm.submit("alice", Game.TICTACTOE, 1500);
        now += 10;
        mm.submit("far", Game.TICTACTOE, 1545);
        now += 10;
        mm.submit("near", Game.TICTACTOE, 1505);
        final Match m = mm.tick().get(0);
        assertEquals("alice", m.playerA());
        assertEquals("near", m.playerB());
        assertEquals(1, mm.queueDepth(Game.TICTACTOE));
    }

    @Test
    void theLongestWaitingPlayerChoosesFirst() {
        mm.submit("old", Game.CHECKERS, 1500);
        now += 10_000;
        mm.submit("new1", Game.CHECKERS, 1700);
        mm.submit("new2", Game.CHECKERS, 1705);
        // new1 and new2 are a perfect pair, but "old" has waited 10 s (window 300) and goes first.
        final List<Match> made = mm.tick();
        assertEquals(1, made.size());
        assertEquals("old", made.get(0).playerA());
        assertEquals("new1", made.get(0).playerB());
    }

    @Test
    void gamesNeverMix() {
        mm.submit("alice", Game.CHECKERS, 1500);
        mm.submit("bob", Game.CONNECT4, 1500);
        assertTrue(mm.tick().isEmpty());
        assertEquals(1, mm.queueDepth(Game.CHECKERS));
        assertEquals(1, mm.queueDepth(Game.CONNECT4));
    }

    @Test
    void aPlayerCanOnlyHaveOneActiveTicket() {
        final Ticket.View first = mm.submit("alice", Game.CHECKERS, 1500);
        final Matchmaker.DuplicateTicketException e = assertThrows(Matchmaker.DuplicateTicketException.class,
                () -> mm.submit("alice", Game.CONNECT4, 1500));
        assertEquals(first.ticketId(), e.existingTicketId());
        mm.cancel(first.ticketId());
        assertNotNull(mm.submit("alice", Game.CONNECT4, 1500));  // free again after cancelling
    }

    @Test
    void cancelRemovesFromTheQueueOnce() {
        final Ticket.View a = mm.submit("alice", Game.CHECKERS, 1500);
        assertEquals(Matchmaker.CancelOutcome.CANCELLED, mm.cancel(a.ticketId()));
        assertEquals(Matchmaker.CancelOutcome.ALREADY_RESOLVED, mm.cancel(a.ticketId()));
        assertEquals(Matchmaker.CancelOutcome.NOT_FOUND, mm.cancel("nope"));
        assertEquals(Ticket.Status.CANCELLED, status(a).status());
        mm.submit("bob", Game.CHECKERS, 1500);
        assertTrue(mm.tick().isEmpty());
    }

    @Test
    void ticketsExpireAfterTheTimeout() {
        final Ticket.View a = mm.submit("alice", Game.CHECKERS, 1000);
        mm.submit("bob", Game.CHECKERS, 2000);  // 1000 apart: never within 400
        now += 59_900;
        mm.tick();
        assertEquals(Ticket.Status.SEARCHING, status(a).status());
        now += 100;
        mm.tick();
        assertEquals(Ticket.Status.EXPIRED, status(a).status());
        assertEquals(List.of(60_000L, 60_000L), expiredWaits);
        assertEquals(0, mm.queueDepth(Game.CHECKERS));
        assertNotNull(mm.submit("alice", Game.CHECKERS, 1000));  // can queue again
    }

    @Test
    void resolvedTicketsAreForgottenAfterTheRetentionPeriod() {
        final Ticket.View a = mm.submit("alice", Game.CHECKERS, 1500);
        mm.submit("bob", Game.CHECKERS, 1500);
        mm.tick();
        now += Matchmaker.Policy.defaults().retainResolvedMillis() + 1;
        mm.tick();
        assertTrue(mm.get(a.ticketId()).isEmpty());
    }

    @Test
    void ifTheDatabaseWriteFailsNobodyIsToldAndBothGoBackInTheQueue() {
        final Ticket.View a = mm.submit("alice", Game.CHECKERS, 1500);
        final Ticket.View b = mm.submit("bob", Game.CHECKERS, 1510);
        failPersist.set(true);
        assertTrue(mm.tick().isEmpty());
        assertEquals(Ticket.Status.SEARCHING, status(a).status());
        assertNull(status(a).matchId());
        assertEquals(2, mm.queueDepth(Game.CHECKERS));
        failPersist.set(false);
        now += 500;
        assertEquals(1, mm.tick().size());
        assertEquals(Ticket.Status.MATCHED, status(b).status());
        assertEquals(500, status(a).waitedMillis());  // kept their original place in line
    }

    /**
     * Property check over a random workload: every player is matched at most once, never across
     * games, never outside the older player's window, and the matches are exactly what was persisted.
     */
    @Test
    void randomWorkloadInvariants() {
        final Random rng = new Random(42);
        final Map<String, Integer> ratings = new HashMap<>();
        final Map<String, Game> games = new HashMap<>();
        final List<Match> all = new ArrayList<>();
        int next = 0;
        for (int step = 0; step < 3_000; step++) {
            for (int k = rng.nextInt(4); k > 0; k--) {
                final String id = "p" + next++;
                final int rating = 1500 + (int) (300 * rng.nextGaussian());
                final Game g = Game.values()[rng.nextInt(Game.values().length)];
                ratings.put(id, rating);
                games.put(id, g);
                mm.submit(id, g, rating);
            }
            now += 100;
            all.addAll(mm.tick());
        }
        final Set<String> seen = new HashSet<>();
        for (Match m : all) {
            assertTrue(seen.add(m.playerA()), "matched twice: " + m.playerA());
            assertTrue(seen.add(m.playerB()), "matched twice: " + m.playerB());
            assertEquals(games.get(m.playerA()), m.game());
            assertEquals(games.get(m.playerB()), m.game());
            assertEquals(ratings.get(m.playerA()), m.ratingA());
            assertTrue(m.waitedMillisA() >= m.waitedMillisB(), "player A is the one who waited longer");
            assertTrue(m.ratingGap() <= mm.policy().window(m.waitedMillisA()), "gap outside window: " + m);
            assertFalse(m.waitedMillisA() > mm.policy().ticketTimeoutMillis());
        }
        assertEquals(all, persisted);
        assertTrue(seen.size() > next * 0.95, "almost everyone gets a game at this arrival rate");
    }
}
