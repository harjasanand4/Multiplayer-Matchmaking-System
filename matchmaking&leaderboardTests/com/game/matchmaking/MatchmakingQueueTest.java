package com.game.matchmaking;

import com.game.leaderboard.Player;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatchmakingQueueTest {

    private Player player1;
    private Player player2;

    private MatchmakingQueue matchmakingQueue;
    @Before
    public void setup(){
        // player 1 setup
        player1.setId(01);
        player1.setCheckersElo(100);

        // player 2 setup
        player2.setId(02);
        player2.setCheckersElo(110);

    }

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
}
