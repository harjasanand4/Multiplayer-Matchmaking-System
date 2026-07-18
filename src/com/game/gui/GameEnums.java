package com.game.gui;

/**
 * The GameEnums file tracks the type of game as well as the match type
 * Purpose: GameType identifies the game while MatchType defines the match mode.
 * These enums are generic and apply to all three games.
 */

public class GameEnums {
    public enum GameType {
        CONNECT_FOUR,
        TIC_TAC_TOE,
        CHECKERS
    }

    public enum MatchType {
        LOCAL,         // Play Local
        CASUAL_ONLINE, // Invite Friend
        RANKED         // Ranked Match
    }
}