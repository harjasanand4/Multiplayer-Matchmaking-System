package com.game.networking;

/**
 * Represents a player in the matchmaking queue.
 */
class QueuedPlayer {
    private final Player player;
    private final GameType gameType;
    private final long queueTime;

    public QueuedPlayer(Player player, GameType gameType, long queueTime) {
        this.player = player;
        this.gameType = gameType;
        this.queueTime = queueTime;
    }

    public Player getPlayer() {
        return player;
    }

    public GameType getGameType() {
        return gameType;
    }

    public long getQueueTime() {
        return queueTime;
    }
}
