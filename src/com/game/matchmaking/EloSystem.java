package com.game.matchmaking;
/**
 * <p>
 *     This Class implements the entire elo ranking algorithm. Implement this to update
 *     actual values after a match.
 * </p>
 * @author Himanshu Basra
 */
public class EloSystem{
    /**
     * calculates the probability of a win for a player
     * @param rA the rating of first player
     * @param rB the rating of second player
     * @return probability of first player winning
     */
    private static double probability(int rA, int rB)
    {
        return 1.0 / ((1 + Math.pow(10.0, rA - rB) / 400.0));
    }

    /**
     * updates the rating of the player after the match has concluded
     * @param ratingA the rating of first player
     * @param ratingB the rating of second player
     * @param constant the win constant determined beforehand implementing a game
     * @param winVar either 1.0, 0.5, or 0 depending on win, draw, or loss
     * @return the updated rating of the first player
     */
    public static int updateRating(int ratingA, int ratingB, int constant, double winVar)
    {
        double winProb = probability(ratingA, ratingB);
        double updatedRating = ratingA + constant*(winVar - winProb);
        return (int) updatedRating;
    }
}