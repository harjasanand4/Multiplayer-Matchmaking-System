package com.game.server.core;

/**
 * Elo ratings.
 *
 * <p>Expected score of A against B is {@code 1 / (1 + 10^((Rb - Ra) / 400))}; after a game A's
 * rating moves by {@code K * (score - expected)}. A 400-point gap means the stronger player is
 * expected to score about 0.91.
 */
public final class Elo {
    public static final int DEFAULT_RATING = 1200;
    /** K-factor while a player has fewer than {@link #PROVISIONAL_GAMES} games: ratings settle faster. */
    public static final int K_PROVISIONAL = 40;
    public static final int K_ESTABLISHED = 24;
    public static final int PROVISIONAL_GAMES = 20;

    private Elo() {}

    public static double expectedScore(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }

    public static int kFactor(int gamesPlayed) {
        return gamesPlayed < PROVISIONAL_GAMES ? K_PROVISIONAL : K_ESTABLISHED;
    }

    /**
     * New rating for a player after one game.
     *
     * @param score 1.0 win, 0.5 draw, 0.0 loss
     */
    public static int updated(int rating, int opponentRating, double score, int k) {
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("score must be between 0 and 1, got " + score);
        }
        return (int) Math.round(rating + k * (score - expectedScore(rating, opponentRating)));
    }
}
