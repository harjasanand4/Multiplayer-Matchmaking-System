-- Applied at startup; every statement is idempotent.

CREATE TABLE IF NOT EXISTS players (
    player_id  TEXT        NOT NULL,
    game       TEXT        NOT NULL,
    rating     INTEGER     NOT NULL,
    games      INTEGER     NOT NULL DEFAULT 0,
    wins       INTEGER     NOT NULL DEFAULT 0,
    losses     INTEGER     NOT NULL DEFAULT 0,
    draws      INTEGER     NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (player_id, game)
);

-- Leaderboard: top N by rating within a game, only players who have finished a game.
CREATE INDEX IF NOT EXISTS players_leaderboard ON players (game, rating DESC) WHERE games > 0;

CREATE TABLE IF NOT EXISTS matches (
    match_id    UUID        PRIMARY KEY,
    game        TEXT        NOT NULL,
    player_a    TEXT        NOT NULL,
    rating_a    INTEGER     NOT NULL,
    player_b    TEXT        NOT NULL,
    rating_b    INTEGER     NOT NULL,
    wait_ms_a   BIGINT      NOT NULL,
    wait_ms_b   BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    result      TEXT        CHECK (result IN ('A', 'B', 'DRAW')),
    reported_by TEXT,
    reported_at TIMESTAMPTZ,
    delta_a     INTEGER,
    delta_b     INTEGER
);

CREATE INDEX IF NOT EXISTS matches_player_a ON matches (player_a, created_at DESC);
CREATE INDEX IF NOT EXISTS matches_player_b ON matches (player_b, created_at DESC);
