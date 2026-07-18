package com.game.matchmaking;


import com.game.leaderboard.Player;

import java.util.ArrayList;

public class MatchmakingQueue {
    private ArrayList<Player> queue = new ArrayList<>();
    public Player popPlayer(){
        if(queue.isEmpty()){
            return null;
        }else{
            return queue.remove(0);
        }
    }
    public void enqueuePlayer(Player player){
        queue.add(player);
    }

    public int getQueueLength(){
        return queue.size();
    }
}
