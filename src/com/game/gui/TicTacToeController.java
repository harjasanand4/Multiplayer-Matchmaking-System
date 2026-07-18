package com.game.gui;

import com.game.gamelogic.TicTacToeLogic;
import com.game.gamelogic.TicTacToePiece;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * TicTacToeController manages the game logic and UI for the Tic Tac Toe game.
 * It handles board creation, managing game state, timing player turns,
 * processing chat messages, and navigating to/from the main menu.
 *
 * Networking team note:
 * The chat functionality currently adds a message to the chat box with an avatar,
 * but the networking integration is not implemented. Use the chat message method
 * as a basis to hook into your networking system for transmitting messages.
 */
public class TicTacToeController {
    // FXML-injected UI elements defined in the TicTacToeScreen.fxml.
    @FXML private Pane gameBoardPane;          // Overall game board container.
    @FXML private Label player1NameLabel;        // Label for Player 1's name.
    @FXML private Label player1RankLabel;        // Label for Player 1's rank.
    @FXML private Label player1TimeLabel;        // Label for Player 1's remaining time.
    @FXML private Rectangle player1TurnIndicator; // Visual indicator for Player 1's turn.
    @FXML private Label player1TurnLabel;        // Label indicating Player 1’s turn.
    @FXML private Label player2NameLabel;        // Label for Player 2's name.
    @FXML private Label player2RankLabel;        // Label for Player 2's rank.
    @FXML private Label player2TimeLabel;        // Label for Player 2's remaining time.
    @FXML private Rectangle player2TurnIndicator; // Visual indicator for Player 2's turn.
    @FXML private Label player2TurnLabel;        // Label indicating Player 2’s turn.
    @FXML private Button requestDrawButton;      // Button to request a draw.
    @FXML private Button forfeitButton;          // Button to forfeit the game.
    @FXML private Label moveCountLabel;          // (Optional) Label to show move count.
    @FXML private Label timeoutStatusLabel;      // Label to display timeout messages.
    @FXML private Label gameTitleLabel;          // Label that shows the game title.
    @FXML private VBox chatMessagesBox;          // Container for chat messages.
    @FXML private TextField chatInputField;      // Text field for typing chat messages.
    @FXML private Button sendChatButton;         // Button to send a chat message.

    // Game logic and board variables.
    private TicTacToeLogic game;                 // Game logic backend.
    private Pane[][] cellPanes;                  // 3x3 array holding the board cells.
    private String player1Name = "Player 1";       // Default name for Player 1.
    private String player2Name = "Player 2";       // Default name for Player 2.

    // Timer variables (a unified 15-second countdown for the active player).
    private Timeline timer;
    private int timeLeft;
    private Label currentTimerLabel;             // Reference to the label showing current time.

    // Constants representing each player's symbol.
    private final String player1Symbol = "X";
    private final String player2Symbol = "O";

    // Images for displaying game pieces.
    private Image xImage;
    private Image oImage;

    // Match mode and type (used to conditionally enable/disable draw requests).
    private String matchMode = "Casual Match";
    private GameEnums.MatchType matchType = GameEnums.MatchType.LOCAL;

    // Chat avatar images (used to display alongside chat messages).
    private Image player1ChatAvatar;
    private Image player2ChatAvatar;

    /**
     * Initializes the controller after FXML elements have been loaded.
     * Sets up game logic, loads images, creates the board, starts the timer,
     * configures UI styling, and wires up the chat button.
     */
    public void initialize() {
        // Initialize game logic with the symbols "X" and "O".
        game = new TicTacToeLogic(player1Symbol, player2Symbol);
        game.startNewGame();

        // Initialize player labels with default names and rank values.
        player1NameLabel.setText(player1Name);
        player2NameLabel.setText(player2Name);
        player1RankLabel.setText("Rank: 1205");
        player2RankLabel.setText("Rank: 1275");
        player1TurnLabel.setText(player1Name + "’s Turn");
        player2TurnLabel.setText(player2Name + "’s Turn");

        // Load the game piece images for "X" and "O".
        try {
            xImage = new Image(getClass().getResourceAsStream("/tic_tac_toe_assets/tictactoe_images/x_icon.png"));
            if (xImage.isError()) {
                throw new RuntimeException("X image failed to load: " + xImage.getException().getMessage());
            }
            oImage = new Image(getClass().getResourceAsStream("/tic_tac_toe_assets/tictactoe_images/o_icon.png"));
            if (oImage.isError()) {
                throw new RuntimeException("O image failed to load: " + oImage.getException().getMessage());
            }
        } catch (Exception e) {
            System.err.println("CRITICAL IMAGE ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        // Load chat avatar images (paths can be adjusted as needed).
        try {
            player1ChatAvatar = new Image(getClass().getResourceAsStream("/game_images/player1-avatar.png"));
            player2ChatAvatar = new Image(getClass().getResourceAsStream("/game_images/player2-avatar.png"));
        } catch (Exception e) {
            System.err.println("Error loading chat avatars: " + e.getMessage());
        }

        // Create the game board dynamically.
        createBoard();
        // Update the UI to highlight the active player's turn.
        updateBoardDisplay(); // Initial board display
        updateTurnIndicators();
        // Start the countdown timer for the active player's turn.
        startTimer(); // Single timer synced with game logic
        // Adjust UI styling based on the match type.
        updateUIToMatchType();

        // Wire up the send chat button action.
        sendChatButton.setOnAction(e -> handleSendChat());

        // Enable sending the chat message when the user presses Enter.
        chatInputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSendChat();
                e.consume();
            }
        });
    }

    /**
     * Creates the Tic Tac Toe board by drawing grid lines and initializing cell panes.
     */
    private void createBoard() {
        // The boardContainer is assumed to be the first child node of gameBoardPane.
        Pane boardContainer = (Pane) gameBoardPane.getChildren().get(0);
        double boardSize = 639;
        double cellSize = boardSize / 3;
        double leftMargin = (690 - boardSize) / 2;

        // Draw horizontal grid lines.
        Line hLine1 = new Line(leftMargin, cellSize, leftMargin + boardSize, cellSize);
        Line hLine2 = new Line(leftMargin, 2 * cellSize, leftMargin + boardSize, 2 * cellSize);
        hLine1.setStrokeWidth(5);
        hLine2.setStrokeWidth(5);
        hLine1.setStroke(Color.WHITE);
        hLine2.setStroke(Color.WHITE);

        // Draw vertical grid lines.
        Line vLine1 = new Line(leftMargin + cellSize, 0, leftMargin + cellSize, boardSize);
        Line vLine2 = new Line(leftMargin + 2 * cellSize, 0, leftMargin + 2 * cellSize, boardSize);
        vLine1.setStrokeWidth(5);
        vLine2.setStrokeWidth(5);
        vLine1.setStroke(Color.WHITE);
        vLine2.setStroke(Color.WHITE);

        // Add the grid lines to the board container.
        boardContainer.getChildren().addAll(hLine1, hLine2, vLine1, vLine2);

        // Create a 3x3 array of cell panes for player moves.
        cellPanes = new Pane[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane cell = new Pane();
                cell.setLayoutX(leftMargin + col * cellSize);
                cell.setLayoutY(row * cellSize);
                cell.setPrefSize(cellSize, cellSize);
                // When a cell is clicked, attempt to make a move.
                final int finalRow = row;
                final int finalCol = col;
                cell.setOnMouseClicked(e -> handleCellClick(finalRow, finalCol));
                cellPanes[row][col] = cell;
                boardContainer.getChildren().add(cell);
            }
        }
    }

    /**
     * Processes a cell click. Attempts to place a piece, updates cell display, and resets timer if necessary.
     */
    private void handleCellClick(int row, int col) {
        if (game.isGameOver()) return;
        String activePlayer = game.getActivePlayer();
        game.placePiece(activePlayer, row, col);
        updateBoardDisplay(); // Update entire board
        if (game.isGameOver()) {
            if (timer != null) timer.stop();
            handleGameOver();
        } else {
            updateTurnIndicators();
        }
    }

    /**
     * Updates the visual display of a board cell, placing an image representing the player's piece.
     */
    private void updateCellDisplay(int row, int col) {
        Pane cellPane = cellPanes[row][col];
        cellPane.getChildren().clear();
        TicTacToePiece piece = (TicTacToePiece) game.getGameBoard().getBoard()[row][col];
        if (piece != null && !piece.getColour().equals("EMPTY")) {
            // Choose the appropriate image based on the piece's symbol.
            ImageView imageView = new ImageView(piece.getColour().equals(player1Symbol) ? xImage : oImage);
            double cellSize = 639 / 3.0;
            imageView.setFitWidth(cellSize * 0.8);
            imageView.setFitHeight(cellSize * 0.8);
            imageView.setLayoutX((cellSize - imageView.getFitWidth()) / 2);
            imageView.setLayoutY((cellSize - imageView.getFitHeight()) / 2);
            cellPane.getChildren().add(imageView);
        }
    }

    private void updateBoardDisplay() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                updateCellDisplay(row, col);
            }
        }
    }

    /**
     * Updates turn indicators (colors and text) to reflect the active player.
     * Also resets the inactive player's timer label to "00:15".
     */
    private void updateTurnIndicators() {
        String activePlayer = game.getActivePlayer();
        Color activeColor = Color.web("#e5e7eb");   // Light gray for active player.
        Color inactiveColor = Color.web("#262626"); // Dark gray for inactive player.
        if (activePlayer.equals(player1Symbol)) {
            // Set player 1 styles for active turn.
            player1TurnIndicator.setFill(activeColor);
            player1TurnLabel.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            // Set player 2 to inactive styling and reset timer display.
            player2TurnIndicator.setFill(inactiveColor);
            player2TurnLabel.setStyle("-fx-text-fill: #262626; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            player2TimeLabel.setText("00:30");
        } else if (activePlayer.equals(player2Symbol)) {
            // Set player 2 styles for active turn.
            player2TurnIndicator.setFill(activeColor);
            player2TurnLabel.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            // Set player 1 to inactive styling and reset timer display.
            player1TurnIndicator.setFill(inactiveColor);
            player1TurnLabel.setStyle("-fx-text-fill: #262626; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            player1TimeLabel.setText("00:30");
        } else {
            System.err.println("Unexpected active player value: " + activePlayer);
        }
    }

    /**
     * Starts the timer for the active player's turn by selecting the appropriate label.
     */
    private void startTimerForActivePlayer() {
        String activePlayer = game.getActivePlayer();
        Label timerLabel = activePlayer.equals(player1Symbol) ? player1TimeLabel : player2TimeLabel;
        startTimer(); // Use single timer method
    }

    /**
     * Starts a 15-second countdown timer and updates the provided label each second.
     */
    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int secondsLeft = game.getSecondsLeft();
            String activePlayer = game.getActivePlayer();
            Label activeTimerLabel = activePlayer.equals(player1Symbol) ? player1TimeLabel : player2TimeLabel;
            activeTimerLabel.setText(String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60));
            if (game.isGameOver()) {
                timer.stop();
                handleGameOver();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    /**
     * Called when a player's turn times out.
     * Displays a timeout message, switches the active player, and restarts the timer.
     */
    private void handleTimeExpired() {
        String expiredPlayer = game.getActivePlayer();
        timeoutStatusLabel.setText("Time expired. " + (expiredPlayer.equals(player1Symbol) ? player1Name : player2Name) + "’s turn skipped.");
        timeoutStatusLabel.setVisible(true);
        Timeline hideMessage = new Timeline(new KeyFrame(Duration.seconds(3), e -> timeoutStatusLabel.setVisible(false)));
        hideMessage.play();
        updateBoardDisplay(); // Reflect any random move made by TicTacToeLogic
        updateTurnIndicators();
    }

    /**
     * Handles game over conditions. Stops the timer, determines the winning message,
     * and shows a dialog with options to play again or return to the main menu.
     */
    private void handleGameOver() {
        if (timer != null) {
            timer.stop();
        }
        String winner = game.getWinner();
        String message;
        if (winner.equals("Draw")) {
            message = "Game ended in a draw!";
            // Re-enable the draw request button.
            requestDrawButton.setDisable(false);
            requestDrawButton.setStyle("-fx-background-color: #a3a3a3; -fx-text-fill: #000000;");
        } else {
            String winningPlayer = winner.equals(player1Symbol) ? player1Name : player2Name;
            message = winningPlayer + " wins!";
        }
        showGameOverDialog(message);
    }

    /**
     * Displays a game over dialog offering to play again or return to the main menu.
     */
    private void showGameOverDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType playAgainButton = new ButtonType("Play Again");
        ButtonType backToMenuButton = new ButtonType("Back to Main Menu");
        alert.getButtonTypes().setAll(playAgainButton, backToMenuButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == playAgainButton) {
                resetGame();
            } else {
                goBackToMainMenu();
            }
        });
    }

    /**
     * Resets the current game state. Clears the board, resets timers, and restarts the game logic.
     */
    private void resetGame() {
        game.startNewGame();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                cellPanes[row][col].getChildren().clear();
            }
        }
        player1TimeLabel.setText("00:30");
        player2TimeLabel.setText("00:30");
        requestDrawButton.setDisable(matchType == GameEnums.MatchType.LOCAL);
        requestDrawButton.setStyle(matchType == GameEnums.MatchType.LOCAL ?
                "-fx-background-color: #262626; -fx-text-fill: #e5e7eb;" :
                "-fx-background-color: #a3a3a3; -fx-text-fill: #000000;");
        updateTurnIndicators();
        startTimer();
    }

    /**
     * Navigates back to the main menu by creating a new scene with the GameMenu_2 controller.
     */
    private void goBackToMainMenu() {
        try {
            GameMenu_2 gameMenu = new GameMenu_2();
            Stage stage = (Stage) gameBoardPane.getScene().getWindow();
            Scene menuScene = gameMenu.createScene(stage);
            stage.setScene(menuScene);
            stage.setTitle("OMG Platform - Game Menu");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) gameBoardPane.getScene().getWindow();
            stage.close();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Draw and Forfeit Action Methods
    // These methods mirror the handling in ConnectFourController, enabling players to request
    // a draw or forfeit the game. Adjust networking logic for RANKED mode as necessary.
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles a draw request when the "Request Draw" button is clicked.
     * Auto-accepts draws in LOCAL and CASUAL_ONLINE modes.
     */
    @FXML
    private void handleDrawRequest() {
        if (matchType == GameEnums.MatchType.LOCAL || matchType == GameEnums.MatchType.CASUAL_ONLINE) {
            // Immediately end the game as a draw.
            game.announceDraw();
            showGameOverDialog("Draw requested and accepted.");
        }
        // TODO: Add networking logic for RANKED mode to send a draw request to the opponent.
    }

    /**
     * Handles the forfeit action when the "Forfeit" button is clicked.
     * Pauses the timer, confirms the decision with the user, and then ends the game.
     */
    @FXML
    private void handleForfeit() {
        if (timer != null) timer.pause();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Forfeit Game");
        confirmation.setHeaderText("Are you sure you want to forfeit?");
        String contentText = "This will end the game immediately.";
        if (matchType == GameEnums.MatchType.RANKED) {
            contentText += " Forfeiting will decrease your Elo rating.";
        }
        confirmation.setContentText(contentText);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(yesButton, noButton);

        confirmation.showAndWait().ifPresent(result -> {
            if (result == yesButton) {
                // Determine which player is forfeiting based on the active symbol.
                String forfeitingPlayer = game.getActivePlayer();
                game.playerResign(forfeitingPlayer);
                String message = (forfeitingPlayer.equals(player1Symbol) ? player1Name : player2Name) + " has forfeited the game.";
                if (matchType == GameEnums.MatchType.RANKED) {
                    // TODO: Implement Elo rating decrease logic here.
                }
                showGameOverDialog(message);
            } else if (timer != null) {
                timer.play();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Chat Handling
    // This method builds an HBox containing the player's avatar and message text.
    // It updates the local chat UI only. Use this as a basis for integrating networking logic.
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles sending a chat message.
     * This method dynamically creates an HBox containing the active player's avatar and chat message,
     * then appends it to the chatMessagesBox VBox (which is defined in the FXML as an empty container).
     * The design preserves your styling choices (the inner chat area background remains #262626,
     * text uses the 'Inter' font and white color) and serves as a basis for further networking integration.
     */
    @FXML
    private void handleSendChat() {
        // Retrieve the text from the chat input field.
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            // Create an HBox with spacing of 8
            HBox chatMessageBox = new HBox();
            chatMessageBox.setSpacing(8.0);

            // Create an ImageView for the avatar and set dimensions.
            ImageView avatarView = new ImageView();
            if (game.getActivePlayer().equals(player1Symbol)) {
                avatarView.setImage(player1ChatAvatar);
            } else {
                avatarView.setImage(player2ChatAvatar);
            }
            avatarView.setFitWidth(32);
            avatarView.setFitHeight(32);

            // Create a VBox that will contain two labels:
            // one for the player's name and one for the chat message.
            VBox textVBox = new VBox();
            // Label for the player's name with the specified style.
            Label nameLabel = new Label(game.getActivePlayer());
            nameLabel.setStyle("-fx-text-fill: #a3a3a3; -fx-font-size: 14px; -fx-font-family: 'Inter';");
            // Label for the message text with the specified style.
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-family: 'Inter';");

            // Add both labels to the VBox.
            textVBox.getChildren().addAll(nameLabel, messageLabel);

            // Add the avatar and text VBox to the HBox.
            chatMessageBox.getChildren().addAll(avatarView, textVBox);

            // Add the complete message HBox to the chat messages container (VBox in the FXML)
            chatMessagesBox.getChildren().add(chatMessageBox);

            // Clear the chat input field for the next message.
            chatInputField.clear();

            // Networking Note:Insert code here to broadcast the message to the opponent/server if needed.
        }
    }

    /**
     * Sets the player names, typically called externally (e.g., by the game menu).
     */
    public void setPlayerNames(String p1Name, String p2Name) {
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        player1NameLabel.setText(p1Name);
        player2NameLabel.setText(p2Name);
        player1TurnLabel.setText(p1Name + "’s Turn");
        player2TurnLabel.setText(p2Name + "’s Turn");
    }

    /**
     * Sets the match type (e.g., LOCAL, CASUAL_ONLINE, RANKED) and updates the UI.
     */
    public void setMatchType(GameEnums.MatchType matchType) {
        this.matchType = matchType;
        if (gameTitleLabel != null) {
            updateUIToMatchType();
        }
    }

    /**
     * Updates UI elements such as the game title and button styling based on the match type.
     */
    private void updateUIToMatchType() {
        gameTitleLabel.setTextFill(Color.web("#e5e7eb"));
        gameTitleLabel.setFont(Font.font("Inter", 23));

        requestDrawButton.setText("Request Draw");
        requestDrawButton.setFont(Font.font("Inter", 19));
        forfeitButton.setText("Forfeit");
        forfeitButton.setFont(Font.font("Inter", 19));

        switch (matchType) {
            case LOCAL:
                gameTitleLabel.setText("Tic Tac Toe - Casual");
                requestDrawButton.setDisable(true);
                requestDrawButton.setStyle(
                        "-fx-background-color: #262626; " +
                                "-fx-text-fill: #e5e7eb; " +
                                "-fx-border-radius: 13; " +
                                "-fx-background-radius: 13;"
                );
                break;
            case CASUAL_ONLINE:
                gameTitleLabel.setText("Tic Tac Toe - Casual");
                requestDrawButton.setDisable(false);
                requestDrawButton.setStyle(
                        "-fx-background-color: #a3a3a3; " +
                                "-fx-text-fill: #000000; " +
                                "-fx-border-radius: 13; " +
                                "-fx-background-radius: 13;"
                );
                break;
            case RANKED:
                gameTitleLabel.setText("Tic Tac Toe - Ranked Match");
                requestDrawButton.setDisable(false);
                requestDrawButton.setStyle(
                        "-fx-background-color: #a3a3a3; " +
                                "-fx-text-fill: #000000; " +
                                "-fx-border-radius: 13; " +
                                "-fx-background-radius: 13;"
                );
                break;
        }
    }
}