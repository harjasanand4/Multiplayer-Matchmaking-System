package com.game.server.store;

import com.game.server.core.Elo;
import com.game.server.core.Game;
import com.game.server.core.Match;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Ratings and match history. {@link InMemoryStore} for tests and local runs, {@link JdbcStore} for Postgres. */
public interface Store extends AutoCloseable {

    record Rating(String playerId, Game game, int rating, int games, int wins, int losses, int draws) {}

    /** Outcome of reporting a result. */
    sealed interface Report {
        record Applied(String matchId, Rating a, Rating b, int deltaA, int deltaB) implements Report {}
        record NotFound() implements Report {}
        record AlreadyReported() implements Report {}
        record NotAParticipant() implements Report {}
    }

    /** The player's rating in a game, creating the player at {@code initialRating} if new. */
    Rating ensurePlayer(String playerId, Game game, int initialRating);

    /** Record new matches (result not yet known). All or nothing. */
    void saveMatches(List<Match> matches);

    /**
     * Apply a result exactly once: the second report for a match is rejected.
     *
     * @param winnerId the winning player, or {@code null} for a draw
     */
    Report reportResult(String matchId, String reportedBy, String winnerId);

    Map<Game, Rating> player(String playerId);

    List<Rating> leaderboard(Game game, int limit);

    Optional<Match> match(String matchId);

    /** Cheap check for the readiness probe. */
    boolean healthy();

    @Override
    default void close() {}

    /** Shared Elo arithmetic so both stores agree to the point. */
    final class Ratings {
        private Ratings() {}

        /** New ratings for A and B; {@code scoreA} is 1 (A won), 0.5 (draw) or 0 (B won). */
        public static Rating[] apply(Rating a, Rating b, double scoreA) {
            final int newA = Elo.updated(a.rating(), b.rating(), scoreA, Elo.kFactor(a.games()));
            final int newB = Elo.updated(b.rating(), a.rating(), 1.0 - scoreA, Elo.kFactor(b.games()));
            return new Rating[] {bump(a, newA, scoreA), bump(b, newB, 1.0 - scoreA)};
        }

        private static Rating bump(Rating r, int rating, double score) {
            return new Rating(r.playerId(), r.game(), rating, r.games() + 1,
                    r.wins() + (score == 1.0 ? 1 : 0), r.losses() + (score == 0.0 ? 1 : 0),
                    r.draws() + (score == 0.5 ? 1 : 0));
        }
    }
}
