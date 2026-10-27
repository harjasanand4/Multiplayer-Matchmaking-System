package com.game.server.core;

/** Two players paired by the matchmaker. Player A is the one who waited longer. */
public record Match(String id, Game game, String playerA, int ratingA, String playerB, int ratingB,
                    long createdAtMillis, long waitedMillisA, long waitedMillisB) {

    public boolean involves(String playerId) {
        return playerA.equals(playerId) || playerB.equals(playerId);
    }

    public int ratingGap() {
        return Math.abs(ratingA - ratingB);
    }
}
