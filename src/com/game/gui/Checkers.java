package com.game.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Checkers class is the main entry point for the Checkers game.
 * It loads the FXML layout for the game screen, initializes the controller,
 * and starts the game. This class (and particularly its inner Board class) handles:
 * - Drawing the board and pieces,
 * - Handling user interactions (clicks, moves, multi-jump logic),
 * - Timing for each player's turn,
 * - Ending the game (win, loss, draw),
 * - And communicating with the UI through the CheckersController.
 *
 * Networking Integration Note:
 * Although the game is currently standalone, the networking team can use
 * the commentary and structure here to understand where to hook in network-related
 * functionality (for example, sending move messages to an opponent,
 * processing draw requests remotely, etc.).
 */
public class Checkers extends Application {

    /**
     * start() is the entry point for the JavaFX application.
     * It loads the FXML layout (CheckersScreen.fxml), retrieves the controller,
     * sets up the scene and stage, and finally starts the game by calling controller.startGame().
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Load the FXML file for the Checkers game screen.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/checkers_assets/CheckersScreen.fxml"));
        Parent root = loader.load();

        // Retrieve the controller and pass in this Checkers instance.
        // This allows the controller to call methods on the game (e.g., resetGame).
        CheckersController controller = loader.getController();
        controller.setGame(this);

        // Create and show the scene.
        Scene scene = new Scene(root, 1440, 1024);
        stage.setTitle("OMG Platform - Checkers");
        stage.setScene(scene);
        stage.show();

        // Start the game logic.
        controller.startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The Board inner class handles the Checkers game logic and UI. It extends GridPane to represent the game board.
     * It contains:
     *  - The board state (gameState[][]),
     *  - The UI tiles (StackPane[][]),
     *  - Methods to initialize and update the board,
     *  - Handlers for user interactions (clicks, piece movement, multi-jump),
     *  - Turn timing via a Timeline,
     *  - And game over logic (win/draw/forfeit).
     *
     * Note for Networking:
     * The Board class represents the "logic" side of the game. If the game were networked,
     * each move or timer update could trigger a network message to synchronize with a remote opponent.
     */
    public class Board extends GridPane {
        private final CheckersController controller; // Reference to the UI controller for updating UI elements.
        private int[][] gameState = new int[8][8]; // The game state grid:
        // 0 = empty, 1 = player1 piece, 2 = player2 piece,
        // 3 = player1 king, 4 = player2 king.
        private StackPane[][] tiles = new StackPane[8][8]; // Graphical tiles corresponding to each board square.
        private int selectedRow = -1; // Row index of the currently selected piece (-1 means none selected).
        private int selectedCol = -1; // Column index of the currently selected piece.
        private int currentPlayer = 1; // Whose turn it is: 1 = Player 1, 2 = Player 2.
        private boolean isJumping = false; // Flag to indicate a multi-jump sequence is in progress.
        private int nonCaptureMoves = 0; // Counter for moves without capture (for draw conditions).
        private int player1Time = 600; // Player 1’s remaining time in seconds.
        private int player2Time = 600; // Player 2’s remaining time in seconds.
        private Timeline timer; // Timer used to update and display remaining time.
        private int player1Captured = 0; // Count of pieces captured by Player 1.
        private int player2Captured = 0; // Count of pieces captured by Player 2.

        // Preload images for pieces and kings. These images are loaded from resources.
        // These paths need to match your project's resources (e.g., under src/main/resources/).
        private final Image player1PieceImage = loadImage("/checkers_assets/checkers_images/moo_cow.png", "Player 1 piece");
        private final Image player2PieceImage = loadImage("/checkers_assets/checkers_images/chicken.png", "Player 2 piece");
        private final Image player1KingImage = loadImage("/checkers_assets/checkers_images/meat.png", "Player 1 king");
        private final Image player2KingImage = loadImage("/checkers_assets/checkers_images/dinner.png", "Player 2 king");

        /**
         * Constructor for the Board.
         * Sets the preferred size and initializes the board.
         */
        public Board(CheckersController controller) {
            this.controller = controller;
            setPrefSize(690, 639);
            setMinSize(690, 639);
            setMaxSize(690, 639);
            setUpBoard();
        }

        /**
         * loadImage() loads an image from the given path.
         * If the image is not found, it prints an error message.
         *
         * @param path the path to the image resource.
         * @param description a description of the image (for debugging).
         * @return an Image object if found, or null otherwise.
         */
        private Image loadImage(String path, String description) {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.out.println("Image not found: " + description + " at " + path);
                return null;
            }
            return new Image(stream);
        }

        /**
         * setUpBoard() builds the game board.
         * It creates StackPane tiles, sets their colors (alternating dark green and black),
         * attaches mouse event handlers for piece selection and movement, and initializes
         * the gameState with pieces for both players.
         */
        private void setUpBoard() {
            double tileWidth = 690.0 / 8.0;
            double tileHeight = 639.0 / 8.0;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    StackPane tile = new StackPane();
                    Rectangle rect = new Rectangle(tileWidth, tileHeight);
                    // Use a checkerboard pattern: black and dark green tiles.
                    rect.setFill((row + col) % 2 == 0 ? Color.BLACK : Color.DARKGREEN);
                    tile.getChildren().add(rect);
                    tiles[row][col] = tile;
                    add(tile, col, row);

                    // Set the tile click event handler.
                    tile.setOnMouseClicked(this::handleTileClick);

                    // Initialize pieces only on dark squares.
                    if ((row + col) % 2 == 1) {
                        if (row < 3) gameState[row][col] = 1; // Player 1 starts at top rows.
                        else if (row > 4) gameState[row][col] = 2; // Player 2 starts at bottom rows.
                    }
                }
            }
            updateBoard();
        }

        /**
         * startGame() begins the game by updating the turn label via the controller
         * and starting the turn timer.
         */
        public void startGame() {
            controller.updateTurnLabel(currentPlayer);
            startTimer();
        }

        /**
         * updateBoard() draws the current state of the board.
         * It iterates over all tiles and, for each tile, adds the appropriate piece (image or circle)
         * based on the value in gameState.
         */
        private void updateBoard() {
            double tileWidth = 690.0 / 8.0;
            double tileHeight = 639.0 / 8.0;
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    StackPane tile = tiles[row][col];
                    // Remove any old piece images or highlights (keeping the background rectangle).
                    if (tile.getChildren().size() > 1)
                        tile.getChildren().remove(1, tile.getChildren().size());

                    int state = gameState[row][col];
                    Image pieceImage = null;
                    // Choose the appropriate image based on the piece value (1, 2, 3, or 4).
                    if (state == 1 && player1PieceImage != null) pieceImage = player1PieceImage;
                    else if (state == 2 && player2PieceImage != null) pieceImage = player2PieceImage;
                    else if (state == 3 && player1KingImage != null) pieceImage = player1KingImage;
                    else if (state == 4 && player2KingImage != null) pieceImage = player2KingImage;

                    if (pieceImage != null) {
                        ImageView piece = new ImageView(pieceImage);
                        piece.setFitWidth(tileWidth * 0.8);
                        piece.setFitHeight(tileHeight * 0.8);
                        tile.getChildren().add(piece);
                    } else if (state != 0) {
                        // If image is missing, fall back to drawing a circle with a solid color.
                        Color pieceColor = (state % 2 == 1) ? Color.RED : Color.GRAY;
                        if (state == 3 || state == 4) pieceColor = pieceColor.brighter();
                        Circle piece = new Circle(tileWidth * 0.4, pieceColor);
                        tile.getChildren().add(piece);
                    }
                }
            }
        }

        /**
         * handleTileClick() is the primary event handler for when a user clicks on a tile.
         * It:
         *   - Determines the grid coordinates of the clicked tile.
         *   - If a multi-jump is in progress (isJumping == true), only processes moves for the selected piece.
         *   - Otherwise, if no piece is selected, it checks if the clicked tile has a piece for the current player.
         *   - If a piece is already selected, it verifies if the clicked tile is a valid move destination.
         * Networking Note:
         *   The click handling here is entirely local. In a networked game, each click might also send a move request over the network.
         */
        private void handleTileClick(MouseEvent event) {
            StackPane tile = (StackPane) event.getSource();
            int row = GridPane.getRowIndex(tile);
            int col = GridPane.getColumnIndex(tile);

            if (isJumping) {
                // During a multi-jump, only allow the previously selected piece to move.
                if (isValidMove(selectedRow, selectedCol, row, col)) {
                    makeMove(selectedRow, selectedCol, row, col);
                }
                return;
            }

            if (selectedRow == -1) {
                // If no piece is selected, select it if it belongs to the current player.
                if (gameState[row][col] != 0 && (gameState[row][col] % 2 == currentPlayer % 2)) {
                    selectPiece(row, col);
                }
            } else {
                // If a piece is already selected, attempt to move it to the clicked tile.
                if (isValidMove(selectedRow, selectedCol, row, col)) {
                    makeMove(selectedRow, selectedCol, row, col);
                } else {
                    // If move is invalid, deselect the piece.
                    deselectPiece();
                }
            }
        }

        /**
         * hasValidFollowUpJump() returns true if, from the specified coordinates,
         * there is at least one valid jump move available. This supports the multi-jump feature.
         */
        private boolean hasValidFollowUpJump(int row, int col) {
            List<int[]> moves = getPossibleMoves(row, col);
            for (int[] move : moves) {
                // Check if the move is a jump by verifying that the row and column differences are 2.
                if (Math.abs(move[0] - row) == 2 && Math.abs(move[1] - col) == 2) {
                    return true;
                }
            }
            return false;
        }

        /**
         * selectPiece() is called when a user clicks on a piece. It stores the piece’s coordinates and highlights possible moves.
         */
        private void selectPiece(int row, int col) {
            selectedRow = row;
            selectedCol = col;
            highlightMoves(row, col);
        }

        /**
         * deselectPiece() clears the selected piece and removes move highlights.
         */
        private void deselectPiece() {
            selectedRow = -1;
            selectedCol = -1;
            clearHighlights();
        }

        /**
         * highlightMoves() iterates over the possible moves for the piece at (row, col)
         * and draws semi-transparent green rectangles on the destination tiles.
         */
        private void highlightMoves(int row, int col) {
            clearHighlights();
            List<int[]> moves = getPossibleMoves(row, col);
            double tileWidth = 690.0 / 8.0;
            double tileHeight = 639.0 / 8.0;
            for (int[] move : moves) {
                StackPane tile = tiles[move[0]][move[1]];
                Rectangle highlight = new Rectangle(tileWidth * 0.8, tileHeight * 0.8, Color.color(0, 1, 0, 0.5));
                tile.getChildren().add(highlight);
            }
        }

        /**
         * clearHighlights() removes move highlight rectangles from all tiles.
         */
        private void clearHighlights() {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    StackPane tile = tiles[r][c];
                    if (tile.getChildren().size() > 1 && tile.getChildren().get(1) instanceof Rectangle) {
                        tile.getChildren().remove(1, tile.getChildren().size());
                    }
                }
            }
        }

        /**
         * getPossibleMoves() calculates and returns a list of valid moves for the piece
         * located at (row, col). Moves include normal moves and jump moves (for capturing).
         *
         * Note: The move calculation uses different directional arrays based on whether
         * the piece is a regular piece or a king.
         */
        private List<int[]> getPossibleMoves(int row, int col) {
            List<int[]> jumpMoves = new ArrayList<>();
            List<int[]> regularMoves = new ArrayList<>();
            int piece = gameState[row][col];
            int[] directions;
            // Regular pieces versus kings.
            if (piece <= 2) {
                directions = (piece == 1) ? new int[]{1, 1, 1, -1} : new int[]{-1, 1, -1, -1};
            } else {
                directions = new int[]{1, 1, 1, -1, -1, 1, -1, -1};
            }
            for (int i = 0; i < directions.length; i += 2) {
                int dr = directions[i];
                int dc = directions[i + 1];
                int newRow = row + dr;
                int newCol = col + dc;
                // Check for a normal (non-capturing) move.
                if (isInBounds(newRow, newCol) && gameState[newRow][newCol] == 0) {
                    regularMoves.add(new int[]{newRow, newCol});
                }
                // Check for a jump move (capturing an opponent's piece).
                int jumpRow = row + 2 * dr;
                int jumpCol = col + 2 * dc;
                int midRow = row + dr;
                int midCol = col + dc;
                if (isInBounds(jumpRow, jumpCol) && gameState[jumpRow][jumpCol] == 0 &&
                        gameState[midRow][midCol] != 0 && gameState[midRow][midCol] % 2 != currentPlayer % 2) {
                    jumpMoves.add(new int[]{jumpRow, jumpCol});
                }
            }
            // According to standard checkers rules, if any capturing move is available, you must capture.
            return (!jumpMoves.isEmpty()) ? jumpMoves : regularMoves;
        }

        /**
         * isInBounds() checks if a set of coordinates is within the board limits.
         */
        private boolean isInBounds(int row, int col) {
            return row >= 0 && row < 8 && col >= 0 && col < 8;
        }

        /**
         * isValidMove() checks if moving a piece from (fromRow, fromCol) to (toRow, toCol)
         * is present in the list of calculated possible moves.
         */
        private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
            List<int[]> moves = getPossibleMoves(fromRow, fromCol);
            for (int[] move : moves) {
                if (move[0] == toRow && move[1] == toCol) return true;
            }
            return false;
        }

        /**
         * makeMove() processes the move of a piece from the source coordinates to the target.
         * It updates the game state, removes captured pieces, promotes to king if necessary,
         * and implements multi-jump logic (keeping the turn if a further jump is available).
         *
         * Networking Note:
         * In a networked game, this method might also transmit the move to the opponent's client.
         */
        private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
            boolean isJump = Math.abs(fromRow - toRow) == 2;

            // Update the game state: move the piece from the source to the target.
            int piece = gameState[fromRow][fromCol];
            gameState[toRow][toCol] = piece;
            gameState[fromRow][fromCol] = 0;

            if (isJump) {
                // Calculate midpoints to identify the captured piece.
                int midRow = (fromRow + toRow) / 2;
                int midCol = (fromCol + toCol) / 2;
                // Remove the captured piece.
                gameState[midRow][midCol] = 0;
                nonCaptureMoves = 0;
                // Update capture counts.
                if (currentPlayer == 1) {
                    player1Captured++;
                } else {
                    player2Captured++;
                }
                controller.updateCapturedCounts(player1Captured, player2Captured);
            } else {
                nonCaptureMoves++;
            }

            // Handle king promotion: if a piece reaches the end of the board.
            if (toRow == 7 && piece == 1) {
                gameState[toRow][toCol] = 3; // Promote Player 1 piece to king.
            }
            if (toRow == 0 && piece == 2) {
                gameState[toRow][toCol] = 4; // Promote Player 2 piece to king.
            }

            // Multi-jump logic: if a jump was made and a follow-up jump is available,
            // do not switch turns; keep the current piece selected.
            if (isJump && hasValidFollowUpJump(toRow, toCol)) {
                isJumping = true;
                selectedRow = toRow;
                selectedCol = toCol;
                updateBoard();
                highlightMoves(toRow, toCol);
                return; // Do not end turn if additional jumps are possible.
            }

            // Otherwise, complete the move by ending the turn.
            isJumping = false;
            deselectPiece();
            updateBoard();
            endTurn();
        }

        /**
         * endTurn() finalizes the current turn by performing the following steps:
         * 1. Updates the UI to reflect the current captured piece counts.
         * 2. Checks for win conditions based on whether either player has captured 12 pieces.
         * 3. Checks for a draw condition if 40 moves have been made with no captures.
         * 4. Verifies whether the opponent has any legal moves left. If not,
         *    it further inspects whether any jump move (capturing move) is available,
         *    because even if regular moves are not available, a jump may still be possible.
         * 5. If no legal move (including jumps) exists for the opponent, the game ends with a “No Moves Left” win.
         * 6. If legal moves remain, the turn is switched to the other player, UI components are updated, and the timer restarts.
         */
        private void endTurn() {
            System.out.println("endTurn() called. currentPlayer before switch: " + currentPlayer);
            controller.updateCapturedCounts(player1Captured, player2Captured);

            // Check win conditions.
            if (player1Captured >= 12) {
                showGameOver("Player 1 Wins!");
                return;
            } else if (player2Captured >= 12) {
                showGameOver("Player 2 Wins!");
                return;
            }
            // Check draw condition: 40 moves with no capture.
            if (nonCaptureMoves >= 40) {
                controller.updateTimerLabels(0, 0);
                stopTimer();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("Draw - 40 Moves Without Capture");
                alert.showAndWait();
                return;
            }
            // Update the UI with the current move count.
            controller.showMoveCount(nonCaptureMoves);

            // Determine the opponent.
            int opponent = (currentPlayer == 1) ? 2 : 1;
            // First, check if the opponent has any legal moves.
            if (!hasLegalMoves(opponent)) {
                // Instead of immediately triggering game over, check if any opponent piece can perform a jump move.
                boolean jumpAvailable = false;
                for (int r = 0; r < 8 && !jumpAvailable; r++) {
                    for (int c = 0; c < 8 && !jumpAvailable; c++) {
                        if (gameState[r][c] != 0 && gameState[r][c] % 2 == opponent % 2) {
                            List<int[]> moves = getPossibleMoves(r, c);
                            for (int[] move : moves) {
                                // A jump move is indicated by a row difference of 2.
                                if (Math.abs(move[0] - r) == 2) {
                                    jumpAvailable = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                // If no jump move is available, declare game over.
                if (!jumpAvailable) {
                    controller.updateTimerLabels(0, 0);
                    stopTimer();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Game Over");
                    alert.setHeaderText(null);
                    alert.setContentText(((currentPlayer == 1) ? "Player 1" : "Player 2") + " Wins - No Moves Left");
                    alert.showAndWait();
                    return;
                }
            }
            // Switch current player.
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
            System.out.println("endTurn() switched turn. New currentPlayer: " + currentPlayer);
            controller.updateTurnLabel(currentPlayer);
            startTimer();
        }

        /**
         * hasLegalMoves() checks whether the specified player has any valid moves left.
         * It iterates over the game board to check for available moves.
         */
        private boolean hasLegalMoves(int player) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    if (gameState[r][c] != 0 && gameState[r][c] % 2 == player % 2) {
                        if (!getPossibleMoves(r, c).isEmpty()) return true;
                    }
                }
            }
            return false;
        }

        /**
         * startTimer() initializes and starts the turn timer.
         * It decrements the timer every second and, if a player's time runs out, ends the game.
         */
        public void startTimer() {
            if (timer != null) timer.stop();
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (currentPlayer == 1) {
                    player1Time--;
                    if (player1Time <= 0) {
                        timer.stop();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Game Over");
                        alert.setHeaderText(null);
                        alert.setContentText("Player 2 Wins - Player 1 Time Out");
                        alert.showAndWait();
                    }
                } else {
                    player2Time--;
                    if (player2Time <= 0) {
                        timer.stop();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Game Over");
                        alert.setHeaderText(null);
                        alert.setContentText("Player 1 Wins - Player 2 Time Out");
                        alert.showAndWait();
                    }
                }
                controller.updateTimerLabels(player1Time, player2Time);
            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
        }

        /**
         * stopTimer() stops the turn timer.
         */
        public void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
        }

        /**
         * getNonCaptureMoves() is a helper method that returns the number of moves without a capture.
         * This is used for determining a draw.
         */
        public int getNonCaptureMoves() {
            return nonCaptureMoves;
        }

        /**
         * announceDraw() stops the timer to signal that the game ended in a draw.
         * In a full implementation, you might add additional logic (like notifying a server).
         */
        public void announceDraw() {
            stopTimer();
        }

        /**
         * resetGame(String, String) reinitializes the board with new player names.
         * It clears the board, resets all counters and timers, then sets up a new board.
         *
         * Networking Note:
         * In an online game, this method might be invoked after a rematch is agreed upon.
         */
        public void resetGame(String player1, String player2) {
            this.getChildren().clear();
            gameState = new int[8][8];
            currentPlayer = 1;
            nonCaptureMoves = 0;
            player1Time = 600;
            player2Time = 600;
            player1Captured = 0;
            player2Captured = 0;
            setUpBoard();
            startTimer();
        }

        /**
         * getCurrentPlayer() returns the identifier for the current active player.
         */
        public int getCurrentPlayer() {
            return currentPlayer;
        }

        /**
         * showGameOver() displays the Game Over dialog with a specified message.
         * It provides two options: "Play Again" or "Main Dashboard." Depending on the choice,
         * it either restarts the game or navigates the user back to the main menu.
         *
         * Networking Note:
         * At game over, networking code would need to finalize the game session and update server records.
         */
        private void showGameOver(String message) {
            stopTimer();
            Alert gameOver = new Alert(Alert.AlertType.CONFIRMATION);
            gameOver.setTitle("Game Over");
            gameOver.setHeaderText(message);
            gameOver.setContentText("Would you like to play again or return to the main dashboard?");
            ButtonType playAgain = new ButtonType("Play Again");
            ButtonType goToDashboard = new ButtonType("Main Dashboard");
            gameOver.getButtonTypes().setAll(playAgain, goToDashboard);
            gameOver.showAndWait().ifPresent(response -> {
                if (response == playAgain) {
                    // Restart the game
                    controller.resetGame();
                } else if (response == goToDashboard) {
                    Platform.runLater(() -> {
                        try {
                            // Direct the user to the main menu screen.
                            GameMenu_2 gameMenu = new GameMenu_2();
                            Stage stage = (Stage) controller.getGameBoardPane().getScene().getWindow();
                            Scene menuScene = gameMenu.createScene(stage);
                            stage.setScene(menuScene);
                            stage.setTitle("OMG Platform - Game Menu");
                            stage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        }

        /**
         * handleDrawRequest() is the method called when the user wishes to request a draw.
         * It shows a confirmation dialog. In a networked game, this would send the draw request
         * to the opponent's client or server for processing.
         */
        public void handleDrawRequest() {
            // Create a confirmation alert asking if the opponent agrees to a draw.
            // In a networked scenario, instead of showing an Alert, you might send a draw request to the server.
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Do you agree to a draw?",
                    ButtonType.YES, ButtonType.NO);

            // Show the alert and block execution until the user responds.
            // In a network context, you might instead wait for a response from the opponent.
            Optional<ButtonType> result = alert.showAndWait();

            // If a response is received and the user chose YES,
            // here we simulate that the opponent agreed to the draw.
            // For networking, you could replace this block with logic that processes the opponent's answer,
            // possibly using a probability-based simulation or a callback from a server.
            if (result.isPresent() && result.get() == ButtonType.YES) {
                // Stop the game timer since the game is ending in a draw.
                timer.stop();

                // Create an informational alert to show that the game has ended as a draw.
                Alert endAlert = new Alert(Alert.AlertType.INFORMATION);
                endAlert.setTitle("Game Over");
                endAlert.setHeaderText(null);
                endAlert.setContentText("Draw - Agreed by Both Players");
                endAlert.showAndWait();
            }
        }
    }
}