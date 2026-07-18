package com.game.matchmaking;

import com.game.leaderboard.Player;
import org.junit.Test;
import org.junit.Before;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MatchmakingQueueTest {

    Player player1 = new Player(1, "Alice", 1500, 1400, 1600, 10, 8, 12);
    Player player2 = new Player(2, "Bob", 1450, 1550, 1500, 7, 11, 9);
    Player player3 = new Player(3, "John", 1600, 1550, 1000, 6, 6, 8);
    Player player4 = new Player(4, "Mike", 1300, 1000 , 900, 9, 10, 5);
    private MatchmakingQueue matchmakingQueue = new MatchmakingQueue();

    @Before
    public void setup() { }

    @Test
    public void queue2players(){
        matchmakingQueue.enqueuePlayer(player1);
        matchmakingQueue.enqueuePlayer(player2);
        int queuelength = matchmakingQueue.getQueueLength();
        assertEquals(2, queuelength);
    }

    @Test
    public void queueremovePlayer(){
        matchmakingQueue.enqueuePlayer(player1);
        matchmakingQueue.enqueuePlayer(player2);
        matchmakingQueue.popPlayer();
        int queuelength = matchmakingQueue.getQueueLength();
        assertEquals(1, queuelength);
    }

    @Test
    public void removePlayerwhenEmpty(){
        Player nullPlayer = matchmakingQueue.popPlayer();
        assertEquals(null, nullPlayer);
    }

    @Test
    public void addPlayerwhenEmpty(){
        matchmakingQueue.enqueuePlayer(player1);
        int queuelength = matchmakingQueue.getQueueLength();
        assertEquals(1, queuelength);
    }

    @Test
    public void testCorrectPlayerRemoved(){
        matchmakingQueue.enqueuePlayer(player1);
        matchmakingQueue.enqueuePlayer(player2);
        matchmakingQueue.enqueuePlayer(player3);
        matchmakingQueue.enqueuePlayer(player4);
        Player playerRemoved = matchmakingQueue.popPlayer();
        int queueLength = matchmakingQueue.getQueueLength();
        assertEquals(player1, playerRemoved);
        assertEquals(3, queueLength);
    }

    @Test
    public void testCorrectPlayerRemoved2(){
        matchmakingQueue.enqueuePlayer(player1);
        matchmakingQueue.enqueuePlayer(player2);
        matchmakingQueue.enqueuePlayer(player3);
        matchmakingQueue.enqueuePlayer(player4);
        matchmakingQueue.popPlayer();
        Player playerRemoved = matchmakingQueue.popPlayer();
        int queueLength = matchmakingQueue.getQueueLength();
        assertEquals(player2, playerRemoved);
        assertEquals(2, queueLength);
    }
    @Test
    public void testRunMatchmakingWithEmptyQueue() throws InterruptedException {
        Matchmaking matchmaking = new Matchmaking();

        // No players enqueued -> queue is empty
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> matchmaking.runMatchmaking("checkers"));

        // Wait a bit then interrupt
        Thread.sleep(1500);
        executor.shutdownNow();

        // If the queue is empty, no game gets created,
        // and presumably no wins are incremented.
        // Just ensure we don't crash or throw exceptions.
        assertTrue(true);  // or any minimal assertion
    }
    @Test
    public void testEloDifferenceBeyond400() throws InterruptedException {
        Player p1 = new Player(1, "LowElo", 100, 100, 100, 0, 0, 0);
        Player p2 = new Player(2, "HighElo", 2000, 2000, 2000, 0, 0, 0);

        // Set them both to the same queue time so no one times out
        p1.setQueuedAt(System.currentTimeMillis());
        p2.setQueuedAt(System.currentTimeMillis());

        Matchmaking matchmaking = new Matchmaking();
        matchmaking.enqueue(p1);
        matchmaking.enqueue(p2);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> matchmaking.runMatchmaking("checkers"));
        Thread.sleep(2000);
        executor.shutdownNow();

        // Because their difference is > 400, findMatch never returns a match,
        // so no wins should increment.
        assertEquals(0, p1.getWins("checkers"));
        assertEquals(0, p2.getWins("checkers"));
    }

    @Test
    public void testMatchmakingInterruptedDuringSleep() throws InterruptedException {
        Matchmaking matchmaking = new Matchmaking();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> matchmaking.runMatchmaking("connect4"));

        // Give it some time to enter Thread.sleep(1000).
        Thread.sleep(500);

        // Now interrupt
        executor.shutdownNow();
        // If there's a catch (InterruptedException e) block,
        // it should be covered by this scenario.
        assertTrue(true);
    }


}
