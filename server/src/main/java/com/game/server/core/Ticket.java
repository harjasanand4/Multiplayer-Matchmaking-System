package com.game.server.core;

/**
 * A player's request to be matched. Mutable state is only changed by the {@link Matchmaker}
 * under its lock; readers see a consistent {@link View}.
 */
public final class Ticket {
    public enum Status { SEARCHING, MATCHED, EXPIRED, CANCELLED }

    private final String id;
    private final String playerId;
    private final Game game;
    private final int rating;
    private final long createdAtMillis;
    private final long sequence;  // join order, breaks ties between tickets created in the same millisecond

    private Status status = Status.SEARCHING;
    private long resolvedAtMillis;
    private String matchId;
    private String opponentId;
    private int opponentRating;

    Ticket(String id, String playerId, Game game, int rating, long createdAtMillis, long sequence) {
        this.id = id;
        this.playerId = playerId;
        this.game = game;
        this.rating = rating;
        this.createdAtMillis = createdAtMillis;
        this.sequence = sequence;
    }

    /** An immutable snapshot for API responses. */
    public record View(String ticketId, String playerId, Game game, int rating, Status status, long waitedMillis,
                       String matchId, String opponentId, Integer opponentRating) {}

    public String id() { return id; }
    public String playerId() { return playerId; }
    public Game game() { return game; }
    public int rating() { return rating; }
    public long createdAtMillis() { return createdAtMillis; }
    long sequence() { return sequence; }
    Status status() { return status; }
    long resolvedAtMillis() { return resolvedAtMillis; }

    void matched(String matchId, Ticket opponent, long now) {
        this.status = Status.MATCHED;
        this.matchId = matchId;
        this.opponentId = opponent.playerId;
        this.opponentRating = opponent.rating;
        this.resolvedAtMillis = now;
    }

    void close(Status terminal, long now) {
        this.status = terminal;
        this.resolvedAtMillis = now;
    }

    View view(long now) {
        final long waited = (status == Status.SEARCHING ? now : resolvedAtMillis) - createdAtMillis;
        return new View(id, playerId, game, rating, status, waited, matchId, opponentId,
                status == Status.MATCHED ? opponentRating : null);
    }
}
