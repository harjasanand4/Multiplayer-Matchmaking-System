package com.game.server.store;

import com.game.server.core.Game;
import com.game.server.core.Match;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL storage.
 *
 * <p>Reporting a result locks the match row and then both player rows ({@code SELECT ... FOR UPDATE},
 * always in player-id order so two concurrent reports can't deadlock), checks the match hasn't been
 * reported already, computes the new ratings and writes everything in one transaction. Two clients
 * racing to report the same match get exactly one {@code Applied} between them.
 */
public final class JdbcStore implements Store {

    /** Database trouble the API turns into a 503. */
    public static final class StoreException extends RuntimeException {
        StoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final ConnectionPool pool;

    public JdbcStore(ConnectionPool pool) {
        this.pool = pool;
    }

    /** Create tables and indexes if they don't exist. */
    public void migrate() {
        final String sql;
        try (InputStream in = JdbcStore.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("db/schema.sql missing from the classpath");
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        run("migrate", c -> {
            try (Statement st = c.createStatement()) {
                for (String statement : statements(sql)) {
                    st.execute(statement);
                }
            }
            return null;
        });
    }

    @Override
    public Rating ensurePlayer(String playerId, Game game, int initialRating) {
        // One round trip on the join path, and no write for a player who already exists: insert if
        // missing, otherwise read the existing row. (The load test showed the join endpoint's two
        // separate statements exhausting the connection pool first.)
        return run("ensurePlayer", c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "WITH inserted AS ("
                            + " INSERT INTO players (player_id, game, rating) VALUES (?, ?, ?) ON CONFLICT DO NOTHING"
                            + " RETURNING player_id, game, rating, games, wins, losses, draws)"
                            + " SELECT * FROM inserted"
                            + " UNION ALL"
                            + " SELECT player_id, game, rating, games, wins, losses, draws FROM players"
                            + " WHERE player_id = ? AND game = ? AND NOT EXISTS (SELECT 1 FROM inserted)")) {
                ps.setString(1, playerId);
                ps.setString(2, game.name());
                ps.setInt(3, initialRating);
                ps.setString(4, playerId);
                ps.setString(5, game.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return toRating(rs);
                    }
                }
            }
            // Rare race: another request inserted the same new player after this statement's
            // snapshot was taken, so neither branch saw a row. A fresh statement will.
            return selectRating(c, playerId, game, false).orElseThrow();
        });
    }

    @Override
    public void saveMatches(List<Match> matches) {
        run("saveMatches", c -> {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO matches (match_id, game, player_a, rating_a, player_b, rating_b, wait_ms_a, wait_ms_b,"
                            + " created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Match m : matches) {
                    ps.setObject(1, UUID.fromString(m.id()));
                    ps.setString(2, m.game().name());
                    ps.setString(3, m.playerA());
                    ps.setInt(4, m.ratingA());
                    ps.setString(5, m.playerB());
                    ps.setInt(6, m.ratingB());
                    ps.setLong(7, m.waitedMillisA());
                    ps.setLong(8, m.waitedMillisB());
                    ps.setTimestamp(9, new Timestamp(m.createdAtMillis()));
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
            return null;
        });
    }

    @Override
    public Report reportResult(String matchId, String reportedBy, String winnerId) {
        final UUID id;
        try {
            id = UUID.fromString(matchId);
        } catch (IllegalArgumentException e) {
            return new Report.NotFound();
        }
        try {
            return pool.inTransaction(c -> {
                final Match m;
                final boolean alreadyReported;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT match_id, game, player_a, rating_a, player_b, rating_b, wait_ms_a, wait_ms_b, created_at,"
                                + " result FROM matches WHERE match_id = ? FOR UPDATE")) {
                    ps.setObject(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return new Report.NotFound();
                        }
                        m = toMatch(rs);
                        alreadyReported = rs.getString("result") != null;
                    }
                }
                if (!m.involves(reportedBy) || (winnerId != null && !m.involves(winnerId))) {
                    return new Report.NotAParticipant();
                }
                if (alreadyReported) {
                    return new Report.AlreadyReported();
                }
                // Lock both rating rows in a fixed order (deadlock-free with concurrent reports).
                final boolean aFirst = m.playerA().compareTo(m.playerB()) <= 0;
                final Rating first = lockedRating(c, aFirst ? m.playerA() : m.playerB(), m.game(),
                        aFirst ? m.ratingA() : m.ratingB());
                final Rating second = lockedRating(c, aFirst ? m.playerB() : m.playerA(), m.game(),
                        aFirst ? m.ratingB() : m.ratingA());
                final Rating a = aFirst ? first : second;
                final Rating b = aFirst ? second : first;

                final double scoreA = winnerId == null ? 0.5 : winnerId.equals(m.playerA()) ? 1.0 : 0.0;
                final Rating[] updated = Ratings.apply(a, b, scoreA);
                writeRating(c, updated[0]);
                writeRating(c, updated[1]);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE matches SET result = ?, reported_by = ?, reported_at = now(), delta_a = ?, delta_b = ?"
                                + " WHERE match_id = ?")) {
                    ps.setString(1, scoreA == 1.0 ? "A" : scoreA == 0.0 ? "B" : "DRAW");
                    ps.setString(2, reportedBy);
                    ps.setInt(3, updated[0].rating() - a.rating());
                    ps.setInt(4, updated[1].rating() - b.rating());
                    ps.setObject(5, id);
                    ps.executeUpdate();
                }
                return new Report.Applied(matchId, updated[0], updated[1], updated[0].rating() - a.rating(),
                        updated[1].rating() - b.rating());
            });
        } catch (SQLException e) {
            throw new StoreException("reportResult failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<Game, Rating> player(String playerId) {
        return run("player", c -> {
            final Map<Game, Rating> out = new EnumMap<>(Game.class);
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT player_id, game, rating, games, wins, losses, draws FROM players WHERE player_id = ?")) {
                ps.setString(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Rating r = toRating(rs);
                        out.put(r.game(), r);
                    }
                }
            }
            return out;
        });
    }

    @Override
    public List<Rating> leaderboard(Game game, int limit) {
        return run("leaderboard", c -> {
            final List<Rating> out = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT player_id, game, rating, games, wins, losses, draws FROM players"
                            + " WHERE game = ? AND games > 0 ORDER BY rating DESC, player_id LIMIT ?")) {
                ps.setString(1, game.name());
                ps.setInt(2, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(toRating(rs));
                    }
                }
            }
            return out;
        });
    }

    @Override
    public Optional<Match> match(String matchId) {
        final UUID id;
        try {
            id = UUID.fromString(matchId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return run("match", c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT match_id, game, player_a, rating_a, player_b, rating_b, wait_ms_a, wait_ms_b, created_at"
                            + " FROM matches WHERE match_id = ?")) {
                ps.setObject(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(toMatch(rs)) : Optional.<Match>empty();
                }
            }
        });
    }

    @Override
    public boolean healthy() {
        try {
            return pool.withConnection(c -> c.isValid(1));
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        pool.close();
    }

    // ---------------------------------------------------------------------------------------------

    private <T> T run(String what, ConnectionPool.Work<T> work) {
        try {
            return pool.withConnection(work);
        } catch (SQLException e) {
            throw new StoreException(what + " failed: " + e.getMessage(), e);
        }
    }

    private static Optional<Rating> selectRating(Connection c, String playerId, Game game, boolean forUpdate)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT player_id, game, rating, games, wins, losses, draws FROM players WHERE player_id = ? AND game = ?"
                        + (forUpdate ? " FOR UPDATE" : ""))) {
            ps.setString(1, playerId);
            ps.setString(2, game.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(toRating(rs)) : Optional.empty();
            }
        }
    }

    private static Rating lockedRating(Connection c, String playerId, Game game, int fallbackRating)
            throws SQLException {
        try (PreparedStatement insert = c.prepareStatement(
                "INSERT INTO players (player_id, game, rating) VALUES (?, ?, ?) ON CONFLICT DO NOTHING")) {
            insert.setString(1, playerId);
            insert.setString(2, game.name());
            insert.setInt(3, fallbackRating);
            insert.executeUpdate();
        }
        return selectRating(c, playerId, game, true).orElseThrow();
    }

    private static void writeRating(Connection c, Rating r) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE players SET rating = ?, games = ?, wins = ?, losses = ?, draws = ?, updated_at = now()"
                        + " WHERE player_id = ? AND game = ?")) {
            ps.setInt(1, r.rating());
            ps.setInt(2, r.games());
            ps.setInt(3, r.wins());
            ps.setInt(4, r.losses());
            ps.setInt(5, r.draws());
            ps.setString(6, r.playerId());
            ps.setString(7, r.game().name());
            ps.executeUpdate();
        }
    }

    private static Rating toRating(ResultSet rs) throws SQLException {
        return new Rating(rs.getString("player_id"), Game.valueOf(rs.getString("game")), rs.getInt("rating"),
                rs.getInt("games"), rs.getInt("wins"), rs.getInt("losses"), rs.getInt("draws"));
    }

    private static Match toMatch(ResultSet rs) throws SQLException {
        return new Match(rs.getObject("match_id").toString(), Game.valueOf(rs.getString("game")),
                rs.getString("player_a"), rs.getInt("rating_a"), rs.getString("player_b"), rs.getInt("rating_b"),
                rs.getTimestamp("created_at").getTime(), rs.getLong("wait_ms_a"), rs.getLong("wait_ms_b"));
    }

    /** Split a schema file into statements on semicolons, dropping comments and blanks. */
    static List<String> statements(String sql) {
        final StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            final int comment = line.indexOf("--");
            clean.append(comment >= 0 ? line.substring(0, comment) : line).append('\n');
        }
        final List<String> out = new ArrayList<>();
        for (String s : clean.toString().split(";")) {
            if (!s.isBlank()) {
                out.add(s.trim());
            }
        }
        return out;
    }
}
