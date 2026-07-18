package com.game.gamelogic;

public class TicTacToeGameStub {
    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private char winner;
    private int moveCount;

    /**
     * Constructor initializes a new game
     */
    public TicTacToeGameStub() {
        initializeGame();
    }

    /**
     * Initialize or reset the game to starting state
     */
    public void initializeGame() {
        // Create a 3x3 board
        board = new char[3][3];

        // Fill board with empty spaces
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = ' ';
            }
        }

        // X always goes first
        currentPlayer = 'X';
        gameOver = false;
        winner = ' ';
        moveCount = 1;
    }

    /**
     * Make a move at the specified position
     *
     * @param row the row index (0-2)
     * @param col the column index (0-2)
     * @return true if move was valid and made, false otherwise
     */
    public boolean makeMove(int row, int col) {
        // Check if the move is valid
        if (row < 0 || row >= 3 || col < 0 || col >= 3 || board[row][col] != ' ' || gameOver) {
            return false;
        }

        // Make the move
        board[row][col] = currentPlayer;

        // Check for win or draw
        checkGameStatus(row, col);

        // If game is not over, switch player
        if (!gameOver) {
            switchPlayer();
        }

        // Increment move count
        moveCount++;

        return true;
    }

    /**
     * Check if the game is over after the last move
     *
     * @param lastRow row of the last move
     * @param lastCol column of the last move
     */
    private void checkGameStatus(int lastRow, int lastCol) {
        // Check row
        if (board[lastRow][0] == currentPlayer && board[lastRow][1] == currentPlayer && board[lastRow][2] == currentPlayer) {
            gameOver = true;
            winner = currentPlayer;
            return;
        }

        // Check column
        if (board[0][lastCol] == currentPlayer && board[1][lastCol] == currentPlayer && board[2][lastCol] == currentPlayer) {
            gameOver = true;
            winner = currentPlayer;
            return;
        }

        // Check diagonals if the move is on a diagonal
        if (lastRow == lastCol) {
            // Check main diagonal (top-left to bottom-right)
            if (board[0][0] == currentPlayer && board[1][1] == currentPlayer && board[2][2] == currentPlayer) {
                gameOver = true;
                winner = currentPlayer;
                return;
            }
        }

        if (lastRow + lastCol == 2) {
            // Check other diagonal (top-right to bottom-left)
            if (board[0][2] == currentPlayer && board[1][1] == currentPlayer && board[2][0] == currentPlayer) {
                gameOver = true;
                winner = currentPlayer;
                return;
            }
        }

        // Check for draw - if all cells are filled
        boolean isDraw = true;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == ' ') {
                    isDraw = false;
                    break;
                }
            }
            if (!isDraw) break;
        }

        if (isDraw) {
            gameOver = true;
        }
    }

    /**
     * Switch the current player
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    /**
     * Get the value of a cell on the board
     *
     * @param row the row index
     * @param col the column index
     * @return the value at the specified position (' ', 'X', or 'O')
     */
    public char getCell(int row, int col) {
        return board[row][col];
    }

    /**
     * Get the current player
     *
     * @return the current player ('X' or 'O')
     */
    public char getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Check if the game is over
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Get the winner of the game
     *
     * @return the winner ('X' or 'O'), or ' ' if there is no winner
     */
    public char getWinner() {
        return winner;
    }

    /**
     * Check if the game ended in a draw
     *
     * @return true if the game ended in a draw, false otherwise
     */
    public boolean isDraw() {
        return gameOver && winner == ' ';
    }

    /**
     * Get the current move count
     *
     * @return the current move count
     */
    public int getMoveCount() {
        return moveCount;
    }
}