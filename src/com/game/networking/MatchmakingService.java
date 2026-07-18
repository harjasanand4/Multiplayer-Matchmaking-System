package com.game.networking;

import java.util.*;
import java.util.concurrent.*;

/**
 * Manages the matchmaking queue and matches players based on different criteria.
 */
public class MatchmakingService {
    private final GameServer gameServer;
    private final Map<String, QueuedPlayer> randomQueue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long QUEUE_CHECK_INTERVAL_MS = 1000; // Check the queue every second

    public MatchmakingService(GameServer gameServer) {
        this.gameServer = gameServer;
        // Start the queue processor
        scheduler.scheduleAtFixedRate(() -> {
            //process the queues to find potential matches
            // but don't automatically match players
            processQueues();
        }, 0, QUEUE_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Represents a pair of matched players
     */
    public static class MatchedPair {
        private final String player1Id;
        private final String player2Id;
        private final GameType gameType;

        public MatchedPair(String player1Id, String player2Id, GameType gameType) {
            this.player1Id = player1Id;
            this.player2Id = player2Id;
            this.gameType = gameType;
        }

        public String getPlayer1Id() { return player1Id; }
        public String getPlayer2Id() { return player2Id; }
        public GameType getGameType() { return gameType; }
    }


    /**
     * Adds a player to the random matchmaking queue.
     * @param player Player to be added to the queue
     * @param gameType Type of game the player wants to play
     */
    public void joinRandomQueue(Player player, GameType gameType) {
        QueuedPlayer queuedPlayer = new QueuedPlayer(player, gameType, System.currentTimeMillis());
        randomQueue.put(player.getID(), queuedPlayer);
        notifyQueueStatus(player, "Searching for a random opponent...");
    }




    /**
     * Removes a player from the random matchmaking queue.
     * @param playerId ID of the player to remove
     */
    public void leaveQueue(String playerId) {
        QueuedPlayer removed = randomQueue.remove(playerId);

        if (removed != null) {
            Player player = removed.getPlayer();
            player.getClientHandler().sendMessage("QUEUE_LEFT|Search canceled");
        }
    }


    /**
     * Processes queue to match players.
     *
     * @return
     */
    private List<MatchedPair> processQueues() {
        // Process random queue - simply pair any two players who want the same game
        processRandomQueue();
        return processRandomQueue();
    }

    /**
     * Processes queue immediately to try to find matches.
     * This is useful when a player just joined the queue.
     * @return List of matched pairs found
     */
    public List<MatchedPair> processImmediateMatch() {
        return processQueues();
    }

    /**
     * Processes the random matchmaking queue.
     */
    private List<MatchedPair> processRandomQueue() {
        Map<GameType, List<QueuedPlayer>> gameTypeGroups = new HashMap<>();
        List<MatchedPair> matchedPairs = new ArrayList<>();

        // Group players by game type
        for (QueuedPlayer qp : randomQueue.values()) {
            gameTypeGroups.computeIfAbsent(qp.getGameType(), k -> new ArrayList<>()).add(qp);
        }

        // For each game type, match pairs of players
        for (Map.Entry<GameType, List<QueuedPlayer>> entry : gameTypeGroups.entrySet()) {
            List<QueuedPlayer> players = entry.getValue();
            GameType gameType = entry.getKey();

            // Sort by wait time (oldest first)
            players.sort(Comparator.comparingLong(QueuedPlayer::getQueueTime));

            // Match pairs
            for (int i = 0; i < players.size() - 1; i += 2) {
                QueuedPlayer player1 = players.get(i);
                QueuedPlayer player2 = players.get(i + 1);

                // Remove from queue
                randomQueue.remove(player1.getPlayer().getID());
                randomQueue.remove(player2.getPlayer().getID());

                // Add to matched pairs
                matchedPairs.add(new MatchedPair(
                        player1.getPlayer().getID(),
                        player2.getPlayer().getID(),
                        gameType
                ));
            }
        }

        return matchedPairs;
    }



    /**
     * Creates a game session and notifies both players.
     */
    private void createMatchAndNotify(Player player1, Player player2, GameType gameType) {
        gameServer.matchPlayers(player1, player2, gameType);
    }


    /**
     * Notifies a player about their match being found.
     */
    public void notifyMatchFound(Player player, Player opponent, String sessionId, GameType gameType) {
        // Format: MATCH_FOUND|sessionId|opponentId|opponentUsername|opponentRank|gameType
        String message = String.format("MATCH_FOUND|%s|%s|%s|%s",
                sessionId,
                opponent.getID(),
                opponent.getUsername(),
                gameType.toString());

        player.getClientHandler().sendMessage(message);
    }

    /**
     * Notifies a player about their current queue status.
     */
    private void notifyQueueStatus(Player player, String statusMessage) {
        player.getClientHandler().sendMessage("QUEUE_STATUS|" + statusMessage);
    }

    /**
     * Shuts down the matchmaking service.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}

