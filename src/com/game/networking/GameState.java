package com.game.networking;


import com.game.gamelogic.Connect4Board;
import com.game.gamelogic.TicTacToeBoard;
import com.game.gamelogic.CheckersBoard;

// basic gameState class (feel free to edit)
public class GameState {
    private final String sessionId;
    private final GameType gameType;
    private final Player player1;
    private final Player player2;
    private CheckersBoard checkersBoard;
    private Connect4Board connect4Board;
    private TicTacToeBoard ticTacToeBoard;

    public GameState(String sessionId, GameType gameType, Player player1, Player player2) {
        this.sessionId = sessionId;
        this.gameType = gameType;
        this.player1 = player1;
        this.player2 = player2;

        if (gameType.equals(GameType.CONNECT4)) {
            Connect4Board board = new Connect4Board();
            board = connect4Board;
        } else if (gameType.equals(GameType.TICTACTOE)) {
            TicTacToeBoard board = new TicTacToeBoard();
            board = ticTacToeBoard;
        } else if (gameType.equals(GameType.CHECKERS)) {
            CheckersBoard board = new CheckersBoard(8, 8);
            board = checkersBoard;
        } else {
            throw new IllegalArgumentException("Unknown gameType: " + gameType);
        }

    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }
    public GameType getGameType() {
        return gameType;
    }
    public Player getPlayer1() {
        return player1;
    }
    public Player getPlayer2() {
        return player2;
    }

}

