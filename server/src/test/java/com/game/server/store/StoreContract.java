package com.game.server.store;

import com.game.server.core.Game;
import com.game.server.core.Match;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The same behaviour is required of the in-memory and the Postgres store. */
abstract class StoreContract {

    abstract Store store();

    private static String id(String base) {
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);  // unique per run: the DB persists
    }

    private Match newMatch(String a, int ra, String b, int rb) {
        final Match m = new Match(UUID.randomUUID().toString(), Game.CHECKERS, a, ra, b, rb,
                System.currentTimeMillis(), 1200, 300);
        store().ensurePlayer(a, Game.CHECKERS, ra);
        store().ensurePlayer(b, Game.CHECKERS, rb);
        store().saveMatches(List.of(m));
        return m;
    }

    @Test
    void newPlayersStartAtTheirPlacementRatingAndKeepIt() {
        final String p = id("new");
        assertEquals(1450, store().ensurePlayer(p, Game.CONNECT4, 1450).rating());
        assertEquals(1450, store().ensurePlayer(p, Game.CONNECT4, 2000).rating());  // hint ignored once known
        assertEquals(1, store().player(p).size());
    }

    @Test
    void aWinMovesBothRatingsByTheEloAmount() {
        final String a = id("a");
        final String b = id("b");
        final Match m = newMatch(a, 1500, b, 1500);
        final Store.Report r = store().reportResult(m.id(), b, a);
        final Store.Report.Applied applied = assertInstanceOf(Store.Report.Applied.class, r);
        assertEquals(20, applied.deltaA());   // K = 40 while provisional, expected 0.5
        assertEquals(-20, applied.deltaB());
        assertEquals(1520, store().player(a).get(Game.CHECKERS).rating());
        assertEquals(1, store().player(a).get(Game.CHECKERS).wins());
        assertEquals(1, store().player(b).get(Game.CHECKERS).losses());
        assertEquals(m.playerA(), store().match(m.id()).orElseThrow().playerA());
    }

    @Test
    void aDrawBetweenUnequalPlayersFavoursTheUnderdog() {
        final String a = id("strong");
        final String b = id("weak");
        final Match m = newMatch(a, 1700, b, 1500);
        final Store.Report.Applied applied = assertInstanceOf(Store.Report.Applied.class,
                store().reportResult(m.id(), a, null));
        assertTrue(applied.deltaA() < 0 && applied.deltaB() > 0);
        assertEquals(1, store().player(b).get(Game.CHECKERS).draws());
    }

    @Test
    void resultsAreAcceptedOnceAndOnlyFromParticipants() {
        final String a = id("a");
        final String b = id("b");
        final Match m = newMatch(a, 1500, b, 1520);
        assertInstanceOf(Store.Report.NotAParticipant.class, store().reportResult(m.id(), "mallory", a));
        assertInstanceOf(Store.Report.NotAParticipant.class, store().reportResult(m.id(), a, "mallory"));
        assertInstanceOf(Store.Report.Applied.class, store().reportResult(m.id(), a, a));
        assertInstanceOf(Store.Report.AlreadyReported.class, store().reportResult(m.id(), b, b));
        assertInstanceOf(Store.Report.NotFound.class, store().reportResult(UUID.randomUUID().toString(), a, a));
        assertInstanceOf(Store.Report.NotFound.class, store().reportResult("not-a-uuid", a, a));
    }

    @Test
    void concurrentReportsForOneMatchApplyExactlyOnce() throws Exception {
        final String a = id("a");
        final String b = id("b");
        final Match m = newMatch(a, 1500, b, 1500);
        final List<Callable<Store.Report>> racers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final String winner = i % 2 == 0 ? a : b;
            racers.add(() -> store().reportResult(m.id(), winner, winner));
        }
        final ExecutorService pool = Executors.newFixedThreadPool(8);
        int applied = 0;
        for (Future<Store.Report> f : pool.invokeAll(racers)) {
            if (f.get() instanceof Store.Report.Applied) {
                applied++;
            }
        }
        pool.shutdown();
        assertEquals(1, applied);
        assertEquals(1, store().player(a).get(Game.CHECKERS).games());
        assertEquals(3000, store().player(a).get(Game.CHECKERS).rating() + store().player(b).get(Game.CHECKERS).rating());
    }

    @Test
    void leaderboardIsSortedAndOnlyShowsPlayersWithGames() {
        final String top = id("top");
        final String mid = id("mid");
        store().ensurePlayer(id("idle"), Game.TICTACTOE, 3000);  // no games: not listed
        final Match m = new Match(UUID.randomUUID().toString(), Game.TICTACTOE, top, 2390, mid, 2380,
                System.currentTimeMillis(), 0, 0);
        store().ensurePlayer(top, Game.TICTACTOE, 2390);
        store().ensurePlayer(mid, Game.TICTACTOE, 2380);
        store().saveMatches(List.of(m));
        store().reportResult(m.id(), top, top);
        final List<Store.Rating> board = store().leaderboard(Game.TICTACTOE, 2);
        assertEquals(top, board.get(0).playerId());
        assertEquals(mid, board.get(1).playerId());
        assertTrue(board.get(0).rating() >= board.get(1).rating());
    }
}
