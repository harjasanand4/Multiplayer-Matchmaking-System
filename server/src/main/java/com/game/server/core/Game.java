package com.game.server.core;

import java.util.Locale;
import java.util.Optional;

/** The games the desktop client offers (same names as {@code com.game.networking.GameType}). */
public enum Game {
    TICTACTOE,
    CHECKERS,
    CONNECT4;

    /** Case-insensitive lookup that accepts "connect4", "Connect4", "CONNECT4". */
    public static Optional<Game> parse(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }
}
