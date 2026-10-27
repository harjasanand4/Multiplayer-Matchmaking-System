package com.game.server.store;

import com.game.server.core.Game;
import com.game.server.core.Match;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Everything in memory, behind one lock. For tests and for running without a database. */
public final class InMemoryStore implements Store {
    private final Map<String, Map<Game, Rating>> players = new HashMap<>();
    private final Map<String, Match> matches = new HashMap<>();
    private final Set<String> reported = new HashSet<>();

    @Override
    public synchronized Rating ensurePlayer(String playerId, Game game, int initialRating) {
        return players.computeIfAbsent(playerId, k -> new EnumMap<>(Game.class))
                .computeIfAbsent(game, g -> new Rating(playerId, g, initialRating, 0, 0, 0, 0));
    }

    @Override
    public synchronized void saveMatches(List<Match> newMatches) {
        for (Match m : newMatches) {
            matches.put(m.id(), m);
        }
    }

    @Override
    public synchronized Report reportResult(String matchId, String reportedBy, String winnerId) {
        final Match m = matches.get(matchId);
        if (m == null) {
            return new Report.NotFound();
        }
        if (!m.involves(reportedBy) || (winnerId != null && !m.involves(winnerId))) {
            return new Report.NotAParticipant();
        }
        if (!reported.add(matchId)) {
            return new Report.AlreadyReported();
        }
        final Rating a = ensurePlayer(m.playerA(), m.game(), m.ratingA());
        final Rating b = ensurePlayer(m.playerB(), m.game(), m.ratingB());
        final double scoreA = winnerId == null ? 0.5 : winnerId.equals(m.playerA()) ? 1.0 : 0.0;
        final Rating[] updated = Ratings.apply(a, b, scoreA);
        players.get(m.playerA()).put(m.game(), updated[0]);
        players.get(m.playerB()).put(m.game(), updated[1]);
        return new Report.Applied(matchId, updated[0], updated[1], updated[0].rating() - a.rating(),
                updated[1].rating() - b.rating());
    }

    @Override
    public synchronized Map<Game, Rating> player(String playerId) {
        return Map.copyOf(players.getOrDefault(playerId, Map.of()));
    }

    @Override
    public synchronized List<Rating> leaderboard(Game game, int limit) {
        return players.values().stream()
                .map(byGame -> byGame.get(game))
                .filter(r -> r != null && r.games() > 0)
                .sorted(Comparator.comparingInt(Rating::rating).reversed().thenComparing(Rating::playerId))
                .limit(limit)
                .toList();
    }

    @Override
    public synchronized Optional<Match> match(String matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    @Override
    public boolean healthy() {
        return true;
    }
}
