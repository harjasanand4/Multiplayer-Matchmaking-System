package com.game.gamelogic;

import java.util.Timer;
import java.util.TimerTask;

public class TicTacToeLogic {

    private TicTacToeBoard GameBoard;
    private String PlayerX;
    private String PlayerO;
    private String ActivePlayer;
    private String Winner;
    private int turnCounter;

    // variables for turn timer
    private Timer TurnTimer;
    private int timeOuts_X;
    private int timeOuts_O;
    private int secondsLeft;
    private final int TURNLENGTH = 30;
    private final int MAXTIMEOUTS = 3;

    public TimerTask newCountdown() {
        return new TimerTask() {
            @Override
            public void run() {
                if (isGameOver()) {
                    return;
                }
                secondsLeft--;
                if (secondsLeft < 0) {
                    if (ActivePlayer.equals(PlayerO)) {
                        timeOuts_O++;
                        if (timeOuts_O == MAXTIMEOUTS) {    // force resignation if they exceed maximum number of timeouts
                            playerResign(PlayerO);
                            TurnTimer.cancel();
                            return;
                        }
                        ActivePlayer = PlayerX;
                    }
                    else {
                        timeOuts_X++;
                        if (timeOuts_X == MAXTIMEOUTS) {    // force resignation if they exceed maximum number of timeouts
                            playerResign(PlayerX);
                            TurnTimer.cancel();
                            return;
                        }
                        ActivePlayer = PlayerO;
                    }
                    // this picks a random empty cell and places the current active player's piece inside it
                    TicTacToePiece[] EmptyCells = GameBoard.getEmptyCells();
                    int randomCell = (int) (Math.random() * (9 - turnCounter));
                    TicTacToePiece Piece = new TicTacToePiece(EmptyCells[randomCell].getxPos(), EmptyCells[randomCell].getyPos(), ActivePlayer);
                    GameBoard.setPiece(Piece);

                    turnCounter++;
                    TurnTimer.cancel(); // stop timer
                    startTurnTimer();
                }
            }
        };
    }


    private void startTurnTimer() {
        if (TurnTimer != null) TurnTimer.cancel(); // Ensure old timer is stopped
        secondsLeft = TURNLENGTH; // reset time
        TurnTimer = new Timer();
        TurnTimer.scheduleAtFixedRate(newCountdown(), 0, 1000);
    }


    public TicTacToeLogic(String Player1, String Player2) {
        this.PlayerX = Player1;
        this.PlayerO = Player2;
        this.Winner = "";
    }

    public void startNewGame() {
        GameBoard = new TicTacToeBoard();
        turnCounter = 0;
        timeOuts_X = 0;
        timeOuts_O = 0;
        secondsLeft = TURNLENGTH;
        if ((int)(Math.random() * 2) == 0) { //choose random player to start
            ActivePlayer = PlayerX;
        }
        else {
            ActivePlayer = PlayerO;
        }
        startTurnTimer();
    }

    public void restartGame() {
        GameBoard = new TicTacToeBoard();
        turnCounter = 0;
        if (Winner.equals(PlayerX)) { //loser goes first
            ActivePlayer = PlayerO;
        }
        else if (Winner.equals(PlayerO)) {
            ActivePlayer = PlayerX;
        }
        else {
            if ((int)(Math.random() * 2) == 0) { //choose random player to start in a tie
                ActivePlayer = PlayerX;
            }
            else {
                ActivePlayer = PlayerO;
            }
        }
        timeOuts_X = 0;
        timeOuts_O = 0;
        startTurnTimer();
        Winner = "";
    }

    public void placePiece(String Player, int x, int y) {
        if (ActivePlayer.equals(Player)) {
            if (GameBoard.canPlay(x, y)) {
                TurnTimer.cancel(); //do first in case of network lag
                TicTacToePiece piece = new TicTacToePiece(x, y, ActivePlayer);
                GameBoard.setPiece(piece); // Bug Fix Piece wasn't being placed on the board

                if (GameBoard.checkIfPlayerWon(new TicTacToePiece(x, y, Player))) {
                    System.out.println("Player " + Player + " has won in placePiece!"); //System message added by GUI team during their implementation
                    announceWinner(Player);
                }
                else {
                    turnCounter++;
                    if (turnCounter == 9) { // board is now full and no-one has won
                        announceDraw();
                    }
                    else {
                        ActivePlayer = (ActivePlayer.equals(PlayerX)) ? PlayerO : PlayerX;
                        startTurnTimer();
                    }
                }
            }
        }
    }

    public void playerResign(String Player) {
        if (!isGameOver()) {
            if (Player.equals(PlayerO)) {
                announceWinner(PlayerX);
            }
            else if (Player.equals(PlayerX)) {
                announceWinner(PlayerO);
            }
            // do nothing in case of wrong player asked to resign
        }
    }

    public void announceWinner(String Player) {
        TurnTimer.cancel();
        ActivePlayer = ""; //set to empty string to make sure there is no overlap with player name
        Winner = Player;
        System.out.println("Winner set to: " + Winner);
    }

    public void announceDraw() {
        TurnTimer.cancel();
        ActivePlayer = "";
        Winner = "Draw";
        System.out.println("Game is a draw");
    }

    public boolean isGameOver() {
        return ActivePlayer.isEmpty();
    }

    public String getWinner() { //added by the GUI team for their implementation
        if (!isGameOver()) {
            return "";
        }
        System.out.println("Returning winner: " + Winner);
        return Winner;
    }

    public String getLoser() { //useful for testing
        if (!isGameOver()) {
            return "";
        }
        return Winner.equals(PlayerX) ? PlayerO : PlayerX;
    }

    public TicTacToeBoard getGameBoard() {
        return GameBoard;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public int getTimeOuts_X() {
        return timeOuts_X;
    }

    public int getTimeOuts_O() {
        return timeOuts_O;
    }

    public String getActivePlayer() {
        return ActivePlayer;
    }

}