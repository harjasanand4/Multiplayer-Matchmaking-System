package com.game.gamelogic;

import java.util.Timer;
import java.util.TimerTask;

public class Connect4Logic {

    public String Player1;
    public String Player2;
    public String ActivePlayer;
    private String Winner = "";
    int p1TimeOut = 0;
    int p2TimeOut = 0;
    private int turnNumber;
    private Connect4Board gameBoard;
    private int gameState = 0;

    private int turnLength = 10;

    Timer turnTime = new Timer();
    TimerTask current;

    public TimerTask makeNew() {
        TimerTask count = new TimerTask() {
            @Override
            public void run() {
                turnLength--;
                if (turnLength < 0) {
                    if (ActivePlayer.equals(Player1)) {
                        gameBoard.autoMove("blue");
                        ActivePlayer = Player2;
                        p1TimeOut++;
                        if (p1TimeOut == 3) {
                            playerResign(Player1);
                        }
                    } else {
                        gameBoard.autoMove("red");
                        ActivePlayer = Player1;
                        p2TimeOut++;
                        if (p2TimeOut == 3) {
                            playerResign(Player2);
                        }
                    }
                    turnLength = 15;
                    // Notify controller of timeout (implementation depends on callback mechanism)
                }
            }
        };
        return count;
    }

    public void startNewGame(String p1, String p2) {
        Player1 = p1;
        Player2 = p2;
        gameBoard = new Connect4Board();
        turnNumber = 0;
        if (Math.round(Math.random()) == 1) {
            ActivePlayer = Player1;
        } else {
            ActivePlayer = Player2;
        }
        turnNumber++;
        if (current != null) current.cancel();
        current = makeNew();
        turnLength = 15;
        turnTime.scheduleAtFixedRate(current, 0, 1000);
    }

    public String getActivePlayer() {
        return ActivePlayer;
    }

    public Boolean placePiece(String Player, int x) {
        if (gameBoard.canPlay(0, x)) {
            if (current != null) current.cancel();
            if (Player.equals(Player1)) {
                gameBoard.play("blue", x);
                if (gameBoard.hasWon("blue")) {
                    Winner = Player1;
                    gameState = 1;
                }
                ActivePlayer = Player2;
            } else {
                gameBoard.play("red", x);
                if (gameBoard.hasWon("red")) {
                    Winner = Player2;
                    gameState = 2;
                }
                ActivePlayer = Player1;
            }
            turnNumber++;

            if (!Winner.isEmpty()) {
                announceWinner(Player);
            } else if (gameBoard.boardFull()) {
                announceDraw();
            } else {
                turnLength = 15;
                current = makeNew();
                turnTime.scheduleAtFixedRate(current, 0, 1000);
            }
            return true;
        }
        return false;
    }

    public void playerResign(String Player) {
        if (Player.equals(Player1)) {
            Winner = Player2;
            gameState = 2;
        } else {
            Winner = Player1;
            gameState = 1;
        }
        announceWinner(Winner);
    }

    public void announceWinner(String Player) {
        System.out.println("Winner is " + Winner);
    }

    public void announceDraw() {
        gameState = 3;
        System.out.println("The game is a draw");
    }

    public boolean isGameOver() {
        return gameState != 0;
    }

    public String getWinner() {
        if (gameState == 3) {
            return "Draw";
        }
        return Winner;
    }

    public Connect4Board getGameBoard() {
        return gameBoard;
    }

    public int getTurnCounter() {
        return turnNumber;
    }

    public int getGameState() {
        return gameState;
    }

    public void setPlayer1(String player1) {
        Player1 = player1;
    }

    public void setPlayer2(String player2) {
        Player2 = player2;
    }

    public void setActivePlayer(String player) {
        ActivePlayer = player;
    }

    public int getTurnLength() {
        return turnLength;
    }
}