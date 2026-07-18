package com.game.gamelogic;

import java.util.Timer;
import java.util.TimerTask;

public class CheckersLogic {
    private String playerWhite;
    private String playerRed;
    private String activePlayer;
    private CheckersBoard board;
    private String winner;
    private int noCaptures;
    private boolean captureInProgress;
    private CheckersPiece capturingPiece;
    private Timer playerRedTimer = new Timer();
    private Timer playerWhiteTimer = new Timer();
    private int pRedSecondsLeft;
    private int pWhiteSecondsLeft;
    private TimerUpdateListener listener;
    private CheckersPiece selectedPiece;
    private int redCapturedPieces;
    private int whiteCapturedPieces;

    TimerTask Countdown = new TimerTask() {
        @Override
        public void run() {
            if (activePlayer.equals(playerRed)) {
                pRedSecondsLeft--;
                if (pRedSecondsLeft < 0) {
                    playerResign(playerRed);
                    playerRedTimer.cancel();
                    playerWhiteTimer.cancel();
                    return;
                }
            }
            else if (activePlayer.equals(playerWhite)) {
                pWhiteSecondsLeft--;
                if (pWhiteSecondsLeft < 0) {
                    playerResign(playerWhite);
                    playerRedTimer.cancel();
                    playerWhiteTimer.cancel();
                    return;
                }
            }
            listener.onTimerUpdate(pRedSecondsLeft, pWhiteSecondsLeft);
        }
    };

    public CheckersLogic(String playerRed, String playerWhite) {
        this.playerWhite = playerWhite;
        this.playerRed = playerRed;
        this.winner = "";
    }

    public void startNewGame() {
        this.board = new CheckersBoard(8, 8);
        board.initializeBoard();
        pRedSecondsLeft = 600;
        pWhiteSecondsLeft = 600;
        if (Math.round(Math.random()) == 1) {
            this.activePlayer = this.playerRed;
            playerRedTimer.scheduleAtFixedRate(Countdown, 0, 1000);
        }else {
            this.activePlayer = this.playerWhite;
            playerWhiteTimer.scheduleAtFixedRate(Countdown, 0, 1000);
        }
        this.winner = "";
        this.redCapturedPieces = 0;
        this.whiteCapturedPieces = 0;
    }

    public String getActivePlayer() {
        return activePlayer;
    }

    public void announceWinner(String player) {
        System.out.println(player + " won the game!");
        this.winner = player;
        this.activePlayer = "";
    }

    public void announceDraw() {
        System.out.println("Draw!");
        this.winner = "Draw";
        this.activePlayer = "";
    }

    public boolean capture(String player, int xStart, int yStart, int xStop, int yStop) {
        if (activePlayer.equals(player)) {
            CheckersPiece piece = this.board.getPiece(xStart, yStart);
            if (capturingPiece == piece) {
                if (board.canPlay(xStop, yStop)) {
                    if (xStop == xStart - 2 && yStop == yStart - 2 && canCaptureUpLeft(piece)) {
                        CheckersPiece remove = board.getPiece(xStart - 1, yStart - 1);
                        this.updatePieces(piece, remove, xStop, yStop);
                        return true;
                    } else if (xStop == xStart + 2 && yStop == yStart - 2 && canCaptureDownLeft(piece)) {
                        CheckersPiece remove = board.getPiece(xStart + 1, yStart - 1);
                        this.updatePieces(piece, remove, xStop, yStop);
                        return true;
                    } else if (xStop == xStart - 2 && yStop == yStart + 2 && canCaptureUpRight(piece)) {
                        CheckersPiece remove = board.getPiece(xStart - 1, yStart + 1);
                        this.updatePieces(piece, remove, xStop, yStop);
                        return true;
                    } else if (xStop == xStart + 2 && yStop == yStart + 2 && canCaptureDownRight(piece)) {
                        CheckersPiece remove = board.getPiece(xStart + 1, yStart + 1);
                        this.updatePieces(piece, remove, xStop, yStop);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean move(String player, int xStart, int yStart, int xStop, int yStop) {
        if (captureInProgress) {
            capture(player, xStart, yStart, xStop, yStop);
            return false;
        }
        if (activePlayer.equals(player)) {
            CheckersPiece piece = this.board.getPiece(xStart, yStart);
            // Check if piece belongs to active player
            if (piece.getColour().equals("r") && activePlayer.equals(playerRed) || piece.getColour().equals("w") && activePlayer.equals(playerWhite)) {
                // Check if player can capture
                boolean activeCanCapture = false;
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (board.getPiece(i, j) != null) {
                            if (activePlayer.equals(playerRed) && board.getPiece(i, j).getColour().equals("r") || activePlayer.equals(playerWhite) && board.getPiece(i, j).getColour().equals("w")) {
                                if (canCapture(board.getPiece(i, j))) {
                                    activeCanCapture = true;
                                }
                            }
                        }
                    }
                }
                if (activeCanCapture && canCapture(piece)) {
                    if (board.canPlay(xStop, yStop)) {
                        if (xStop == xStart - 2 && yStop == yStart - 2 && canCaptureUpLeft(piece)) {
                            noCaptures = 0;
                            CheckersPiece remove = board.getPiece(xStart - 1, yStart - 1);
                            this.updatePieces(piece, remove, xStop, yStop);
                            return true;
                        } else if (xStop == xStart + 2 && yStop == yStart - 2 && canCaptureDownLeft(piece)) {
                            noCaptures = 0;
                            CheckersPiece remove = board.getPiece(xStart + 1, yStart - 1);
                            this.updatePieces(piece, remove, xStop, yStop);
                            return true;
                        } else if (xStop == xStart - 2 && yStop == yStart + 2 && canCaptureUpRight(piece)) {
                            noCaptures = 0;
                            CheckersPiece remove = board.getPiece(xStart - 1, yStart + 1);
                            this.updatePieces(piece, remove, xStop, yStop);
                            return true;
                        } else if (xStop == xStart + 2 && yStop == yStart + 2 && canCaptureDownRight(piece)) {
                            noCaptures = 0;
                            CheckersPiece remove = board.getPiece(xStart + 1, yStart + 1);
                            this.updatePieces(piece, remove, xStop, yStop);
                            return true;
                        }
                    }
                } else if (!activeCanCapture && !canCapture(piece)) {
                    if (board.canPlay(xStop, yStop)) {
                        if (piece.isKing() || (activePlayer.equals(playerRed) && xStop < piece.getxPos())|| (activePlayer.equals(playerWhite) && xStop > piece.getxPos())) {
                            if (xStop == xStart - 1 && yStop == yStart - 1 || xStop == xStart + 1 && yStop == yStart - 1 || xStop == xStart - 1 && yStop == yStart + 1 || xStop == xStart + 1 && yStop == yStart + 1) {
                                board.removePiece(piece);
                                piece.setxPos(xStop);
                                piece.setyPos(yStop);
                                board.setPiece(piece);
                                if (piece.getColour().equals("r") && xStop == 0) {
                                    piece.setKing(true);
                                }
                                if (piece.getColour().equals("w") && xStop == 7) {
                                    piece.setKing(true);
                                }
                                if (activePlayer.equals(playerRed)) {
                                    activePlayer = playerWhite;
                                } else {
                                    activePlayer = playerRed;
                                }
                                noCaptures++;
                                if (noCaptures >= 40) {
                                    this.announceDraw();
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void selectPiece(String player, int row, int col) {
        if (this.selectedPiece != null) {
            if (board.getPiece(row, col) == null) {
                move(player, selectedPiece.getxPos(), selectedPiece.getyPos(), row, col);
            }
        }
        this.selectedPiece = board.getPiece(row, col);
    }

    private void updatePieces(CheckersPiece piece, CheckersPiece remove, int xStop, int yStop) {
        board.removePiece(piece);
        piece.setxPos(xStop);
        piece.setyPos(yStop);
        board.removePiece(remove);
        board.setPiece(piece);
        if (piece.getColour().equals("r") && xStop == 0) {
            piece.setKing(true);
        }
        if (piece.getColour().equals("w") && xStop == 7) {
            piece.setKing(true);
        }
        if (activePlayer.equals(playerRed)) {
            redCapturedPieces ++;
        }
        else if (activePlayer.equals(playerWhite)) {
            whiteCapturedPieces ++;
        }
        if (canCapture(piece)) {
            captureInProgress = true;
            capturingPiece = piece;
        } else {
            captureInProgress = false;
            if (activePlayer.equals(playerRed)) {
                activePlayer = playerWhite;
            } else {
                activePlayer = playerRed;
            }
        }
    }

    public void playerResign(String player) {
        if (player.equals(playerWhite)) {
            this.announceWinner(playerRed);
        }
        else {
            this.announceWinner(playerWhite);
        }
    }

    public boolean canCapture(CheckersPiece piece) {
        if (!piece.isKing()) {
            if (piece.getColour().equals("r")) {
                if (this.canCaptureUpLeft(piece) || this.canCaptureUpRight(piece)) {
                    return true;
                }
            }
            else {
                if (this.canCaptureDownLeft(piece) || this.canCaptureDownRight(piece)) {
                    return true;
                }
            }
        }
        else {
            if (this.canCaptureUpLeft(piece) || this.canCaptureUpRight(piece) || this.canCaptureDownLeft(piece) || this.canCaptureDownRight(piece)) {
                return true;
            }
        }
        return false;
    }

    private boolean canCaptureUpLeft(CheckersPiece piece) {
        if (this.board.canPlay(piece.getxPos()-2, piece.getyPos()-2)) {
            CheckersPiece opponent = this.board.getPiece(piece.getxPos()-1, piece.getyPos()-1);
            if (opponent != null) {
                if (!opponent.getColour().equals(piece.getColour())) {
                    System.out.println("canCaptureUpLeft");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canCaptureUpRight(CheckersPiece piece) {
        if (this.board.canPlay(piece.getxPos()-2, piece.getyPos()+2)) {
            CheckersPiece opponent = this.board.getPiece(piece.getxPos()-1, piece.getyPos()+1);
            if (opponent != null) {
                if (!opponent.getColour().equals(piece.getColour())) {
                    System.out.println("canCaptureUpRight");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canCaptureDownLeft(CheckersPiece piece) {
        if (this.board.canPlay(piece.getxPos()+2, piece.getyPos()-2)) {
            CheckersPiece opponent = this.board.getPiece(piece.getxPos()+1, piece.getyPos()-1);
            if (opponent != null) {
                if (!opponent.getColour().equals(piece.getColour())) {
                    System.out.println("canCaptureDownLeft");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canCaptureDownRight(CheckersPiece piece) {
        if (this.board.canPlay(piece.getxPos()+2, piece.getyPos()+2)) {
            CheckersPiece opponent = this.board.getPiece(piece.getxPos()+1, piece.getyPos()+1);
            if (opponent != null) {
                if (!opponent.getColour().equals(piece.getColour())) {
                    System.out.println("canCaptureDownRight");
                    return true;
                }
            }
        }
        return false;
    }

    public int getNoCaptures() {
        return noCaptures;
    }

    public CheckersBoard getBoard() {
        return board;
    }

    public void setTimerUpdateListener(TimerUpdateListener listener) {
        this.listener = listener;
    }

    public int getRedCaptures() {
        return redCapturedPieces;
    }

    public int getWhiteCaptures() {
        return whiteCapturedPieces;
    }

    public String checkWin() {
        if (activePlayer.isEmpty()) {
            return winner;
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = this.board.getPiece(i, j);
                if (piece != null) {
                    if (activePlayer.equals(playerRed) && piece.getColour().equals("r") || activePlayer.equals(playerWhite) && piece.getColour().equals("w")) {
                        if (canCapture(piece)) {
                            return null;
                        }
                        if (board.canPlay(piece.getxPos()-1, piece.getyPos()-1) || board.canPlay(piece.getxPos()-1, piece.getyPos()+1) || board.canPlay(piece.getxPos()+1, piece.getyPos()-1) || board.canPlay(piece.getxPos()+1, piece.getyPos()+1)) {
                            return null;
                        }
                    }
                }
            }
        }
        if (activePlayer.equals(playerWhite)) {
            this.announceWinner(playerRed);
            playerRedTimer.cancel();
            playerWhiteTimer.cancel();
            return playerRed;
        }
        else {
            this.announceWinner(playerWhite);
            playerRedTimer.cancel();
            playerWhiteTimer.cancel();
            return playerWhite;
        }

    }

    public String getWinner() {
        return winner;
    }
}