package com.game.gui;

import com.game.gamelogic.Connect4Board;
import com.game.gamelogic.Connect4Logic;
import com.game.gamelogic.Connect4Piece;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * ConnectFourController manages the game logic and UI for the Connect Four game screen.
 * It handles player moves, timers, game state, UI updates, and user interactions like draw requests,
 * forfeits, and chat functionality. The controller is tied to Connect4Screen.fxml.
 */
public class ConnectFourController {

    // FXML-injected UI elements from Connect4Screen.fxml
    @FXML private Label drawStatusLabel;        // Label to show draw request status (e.g., "Draw Proposed").
    @FXML private Pane gameBoardPane;           // Outer pane containing the game board.
    @FXML private Pane boardPane;               // Inner pane where the Connect Four grid is drawn.
    @FXML private Label player1NameLabel;       // Displays Player 1's name.
    @FXML private Label player1RankLabel;       // Displays Player 1's rank (e.g., "Rank: 1205").
    @FXML private Label player1TimeLabel;       // Shows Player 1's remaining time per turn.
    @FXML private Rectangle player1TurnIndicator; // Visual indicator for Player 1's turn (changes color).
    @FXML private Label player1TurnLabel;       // Text indicating Player 1's turn (e.g., "Player 1’s Turn").
    @FXML private ImageView player1PieceImage;  // Image of Player 1's game piece (red or blue).
    @FXML private Label player2NameLabel;       // Displays Player 2's name.
    @FXML private Label player2RankLabel;       // Displays Player 2's rank (e.g., "Rank: 1275").
    @FXML private Label player2TimeLabel;       // Shows Player 2's remaining time per turn.
    @FXML private Rectangle player2TurnIndicator; // Visual indicator for Player 2's turn (changes color).
    @FXML private Label player2TurnLabel;       // Text indicating Player 2's turn (e.g., "Player 2’s Turn").
    @FXML private ImageView player2PieceImage;  // Image of Player 2's game piece (red or blue).
    @FXML private Button requestDrawButton;     // Button to request a draw.
    @FXML private Button forfeitButton;         // Button to forfeit the game.
    @FXML private Label timeoutStatusLabel;     // Label to show timeout messages (e.g., "Time expired").
    @FXML private Label gameTitleLabel;         // Title label (e.g., "Connect Four - Casual").
    @FXML private VBox chatMessagesBox;         // Container for chat messages.
    @FXML private TextField chatInputField;     // Input field for typing chat messages.
    @FXML private Button sendChatButton;        // Button to send chat messages.
    @FXML private Label networkTextLabel;
    @FXML private ImageView networkIcon;

    // Game logic and state variables
    private Connect4Logic game;                 // Backend logic for Connect Four (board state, rules, etc.).
    private Circle[][] cellCircles;             // 2D array of Circle objects representing the game board grid.
    private String player1Name = "Player 1";    // Default name for Player 1, updated via setPlayerNames().
    private String player2Name = "Player 2";    // Default name for Player 2, updated via setPlayerNames().
    private Timeline timer;                     // Timer for tracking turn time limits.
    private int timeLeft;                       // Remaining time in seconds for the current player's turn.
    private Label currentTimerLabel;            // Reference to the active player's timer label.
    private GameEnums.MatchType matchType = GameEnums.MatchType.LOCAL; // Match type (LOCAL, CASUAL_ONLINE, RANKED).

    // Chat avatar images for both players.
    private Image player1ChatAvatar;
    private Image player2ChatAvatar;

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up the game, UI elements, board, timers, and event handlers.
     */
    public void initialize() {
        // Check for null FXML elements
        if (gameBoardPane == null || boardPane == null || player1NameLabel == null || player2NameLabel == null) {
            System.err.println("Error: One or more FXML elements are not properly initialized.");
            return;
        }

        // Initialize the game logic with default player names.
        game = new Connect4Logic();
        game.startNewGame(player1Name, player2Name); // Start a new game (resets board, assigns colors).

        // Set initial UI labels with player names and ranks (hardcoded ranks for now).
        player1NameLabel.setText(player1Name);
        player2NameLabel.setText(player2Name);
        player1RankLabel.setText("Rank: 1205");
        player2RankLabel.setText("Rank: 1275");
        player1TurnLabel.setText(player1Name + "’s Turn");
        player2TurnLabel.setText(player2Name + "’s Turn");

        // Load player piece images.
        updatePlayerPieceImages();

        // Create the Connect Four board (6 rows x 7 columns).
        cellCircles = new Circle[6][7];
        createBoard();

        // Update turn indicators and start the turn timer.
        updateTurnIndicators();
        startTimerForActivePlayer();
        updateUIToMatchType();

        // Load chat avatar images for the players.
        try {
            player1ChatAvatar = new Image(getClass().getResourceAsStream("/game_images/player1-avatar.png"));
            player2ChatAvatar = new Image(getClass().getResourceAsStream("/game_images/player2-avatar.png"));
        } catch (Exception e) {
            System.err.println("Error loading chat avatars: " + e.getMessage());
        }

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
     * Sets the match type (LOCAL, CASUAL_ONLINE, RANKED) and updates the UI accordingly.
     * Called by GameMenu_2 when launching the game.
     * @param matchType The type of match to set.
     */
    public void setMatchType(GameEnums.MatchType matchType) {
        this.matchType = matchType;
        if (gameTitleLabel != null) { // Ensure UI is initialized before updating.
            updateUIToMatchType();
        }
    }

    /**
     * Updates UI elements based on the match type.
     * Adjusts the game title and draw button behavior to reflect the match type.
     */
    private void updateUIToMatchType() {
        // Set game title styling explicitly to match FXML
        gameTitleLabel.setTextFill(Color.web("#e5e7eb"));
        gameTitleLabel.setFont(Font.font("Inter", 23));

        // Base styling for requestDrawButton to match FXML
        requestDrawButton.setText("Request Draw"); // Ensure text matches FXML
        requestDrawButton.setFont(Font.font("Inter", 19)); // Match font and size
        // Icon is already set in FXML, no need to change

        // Base styling for forfeitButton to match FXML
        forfeitButton.setText("Forfeit");
        forfeitButton.setFont(Font.font("Inter", 19));

        switch (matchType) {
            case LOCAL:
                gameTitleLabel.setText("Connect Four - Casual Match");
                requestDrawButton.setDisable(true);
                requestDrawButton.setStyle(
                        "-fx-background-color: #262626; " + // Disabled: dark gray (matches FXML default)
                                "-fx-text-fill: #e5e7eb; " +       // Light gray text (matches FXML)
                                "-fx-border-radius: 13; " +        // Match FXML radius
                                "-fx-background-radius: 13;"       // Match FXML radius
                );
                break;
            case CASUAL_ONLINE:
                gameTitleLabel.setText("Connect Four - Casual Match");
                requestDrawButton.setDisable(false);
                requestDrawButton.setStyle(
                        "-fx-background-color: #a3a3a3; " + // Enabled: medium gray
                                "-fx-text-fill: #000000; " +       // Black text for contrast
                                "-fx-border-radius: 13; " +        // Match FXML radius
                                "-fx-background-radius: 13;"       // Match FXML radius
                );
                break;
            case RANKED:
                gameTitleLabel.setText("Connect Four - Ranked Match");
                requestDrawButton.setDisable(false);
                requestDrawButton.setStyle(
                        "-fx-background-color: #a3a3a3; " + // Enabled: medium gray
                                "-fx-text-fill: #000000; " +       // Black text for contrast
                                "-fx-border-radius: 13; " +        // Match FXML radius
                                "-fx-background-radius: 13;"       // Match FXML radius
                );
                break;
        }
    }

    /**
     * Updates player names and resets the game with the new names.
     * Called by GameMenu_2 to set player names based on match type.
     * @param p1Name Player 1's name.
     * @param p2Name Player 2's name.
     */
    public void setPlayerNames(String p1Name, String p2Name) {
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        // Update UI labels with new names.
        player1NameLabel.setText(p1Name);
        player2NameLabel.setText(p2Name);
        player1TurnLabel.setText(p1Name + "’s Turn");
        player2TurnLabel.setText(p2Name + "’s Turn");
        // Restart the game with new player names.
        game = new Connect4Logic();
        game.startNewGame(p1Name, p2Name);
        updatePlayerPieceImages();
        updateBoardDisplay();
        updateUIToMatchType();
    }

    /**
     * Creates the 6x7 Connect Four board using Circle objects for cells and Rectangles for clickable columns.
     */
    private void createBoard() {
        double width = 690;  // Board width in pixels.
        double height = 639; // Board height in pixels.
        double margin = 20;  // Margin around the board.
        double radius = 40;  // Radius of each cell (circle).
        double spacing = (width - 2 * margin - 2 * radius) / 6; // Spacing between cells.

        // Create the 6x7 grid of circles (cells).
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                double x = margin + radius + col * spacing; // X position of the cell.
                double y = margin + radius + row * spacing; // Y position of the cell.
                Circle cell = new Circle(x, y, radius);
                cell.setFill(Color.LIGHTGRAY); // Default empty cell color.
                cell.setStroke(Color.BLACK);   // Black border for visibility.
                cell.setStrokeWidth(2);
                cellCircles[row][col] = cell;  // Store in array for later updates.
                boardPane.getChildren().add(cell); // Add to the board pane.
            }
        }

        // Create invisible rectangles over each column for click detection.
        for (int col = 0; col < 7; col++) {
            double x = margin + col * spacing;
            Rectangle columnRect = new Rectangle(x, 0, spacing, height);
            columnRect.setFill(Color.TRANSPARENT); // Invisible but clickable.
            final int finalCol = col;
            columnRect.setOnMouseClicked(e -> handleColumnClick(finalCol)); // Handle clicks on this column.
            boardPane.getChildren().add(columnRect);
        }

        // Update the board display to reflect the initial game state.
        updateBoardDisplay();
    }

    /**
     * Updates the visual representation of the game board based on the game state.
     * Colors each cell according to the piece (red, blue, or empty).
     */
    private void updateBoardDisplay() {
        Connect4Board board = game.getGameBoard();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                Connect4Piece piece = (Connect4Piece) board.board[row][col];
                Circle cell = cellCircles[row][col];
                if (piece != null) {
                    if ("blue".equals(piece.getColour())) {
                        cell.setFill(Color.BLUE);
                    } else if ("red".equals(piece.getColour())) {
                        cell.setFill(Color.RED);
                    } else {
                        System.err.println("Invalid piece color: " + piece.getColour());
                        cell.setFill(Color.LIGHTGRAY);
                    }
                } else {
                    cell.setFill(Color.LIGHTGRAY); // Empty cell.
                }
            }
        }
    }

    /**
     * Handles a player's move when a column is clicked.
     * @param col The column index (0-6) where the player clicked.
     */
    private void handleColumnClick(int col) {
        if (game.getActivePlayer().equals("None")) return; // Ignore clicks if game is over.
        String previousPlayer = game.getActivePlayer();
        game.placePiece(previousPlayer, col); // Attempt to place a piece in the column.
        updateBoardDisplay(); // Reflect the new piece on the board.
        if (game.getActivePlayer().equals("None") || game.getGameState() != 0) {
            handleGameOver(); // Game ended (win or draw).
        } else {
            // Update timer display
            updateTurnIndicators();
            updateTimerDisplay();
        }
    }

    /**
     * Starts the timer for the current active player.
     */
    private void startTimerForActivePlayer() {
        updateTimerDisplay();
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimerDisplay()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    /**
     * Updates the timer display based on the game logic's turn length.
     */
    private void updateTimerDisplay() {
        String activePlayer = game.getActivePlayer();
        Label timerLabel = activePlayer.equals(game.Player1) ? player1TimeLabel : player2TimeLabel;
        int timeLeft = game.getTurnLength();
        timerLabel.setText(String.format("%02d:%02d", timeLeft / 60, timeLeft % 60));
        currentTimerLabel = timerLabel;
    }

    /**
     * Handles the case when a player's turn time expires.
     * Skips the player's turn and switches to the other player.
     */
    private void handleTimeExpired() {
        String expiredPlayer = (currentTimerLabel == player1TimeLabel) ? player1Name : player2Name;
        timeoutStatusLabel.setText("Time expired. " + expiredPlayer + "’s turn skipped.");
        timeoutStatusLabel.setVisible(true);
        // Hide the timeout message after 3 seconds.
        Timeline hideMessage = new Timeline(new KeyFrame(Duration.seconds(3), e -> timeoutStatusLabel.setVisible(false)));
        hideMessage.play();

        // Update UI
        updateTurnIndicators();
        updateTimerDisplay();
    }

    /**
     * Updates the player piece images (red/blue) based on the game logic.
     * Player 1 and Player 2 are assigned colors at the start of the game.
     */
    private void updatePlayerPieceImages() {
        try {
            // Verify resource paths
            String bluePath = "/connect4_assets/connect4_images/blue_piece.png";
            String redPath = "/connect4_assets/connect4_images/red_piece.png";
            java.io.InputStream blueStream = getClass().getResourceAsStream(bluePath);
            java.io.InputStream redStream = getClass().getResourceAsStream(redPath);
            if (blueStream == null) {
                throw new IllegalArgumentException("Cannot find resource: " + bluePath);
            }
            if (redStream == null) {
                throw new IllegalArgumentException("Cannot find resource: " + redPath);
            }
            Image bluePieceImage = new Image(blueStream);
            Image redPieceImage = new Image(redStream);
            player1PieceImage.setImage(bluePieceImage);    // Player 1 with blue
            player2PieceImage.setImage(redPieceImage);     // Player 2 with red
        } catch (Exception e) {
            // Handle image loading errors gracefully.
            System.err.println("Error loading piece images: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default images or log error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image Loading Error");
            alert.setHeaderText("Failed to load game piece images");
            alert.setContentText("Please ensure blue_piece.png and red_piece.png are in the correct directory: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Updates the turn indicators to highlight the active player.
     * Changes the color of the turn indicator and label text.
     */
    private void updateTurnIndicators() {
        String activePlayer = game.getActivePlayer();
        player1TurnLabel.setStyle("");
        player2TurnLabel.setStyle("");

        if (activePlayer.equals(game.Player1)) {
            // Highlight Player 1's turn.
            player1TurnIndicator.setFill(Color.WHITE);
            player1TurnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            player2TurnIndicator.setFill(Color.web("#262626")); // Dark gray for inactive.
            player2TurnLabel.setStyle("-fx-text-fill: #262626; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
        } else {
            // Highlight Player 2's turn.
            player2TurnIndicator.setFill(Color.WHITE);
            player2TurnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            player1TurnIndicator.setFill(Color.web("#262626"));
            player1TurnLabel.setStyle("-fx-text-fill: #262626; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
        }
    }

    /**
     * Handles a draw request from the active player.
     * Currently, auto-accepts in LOCAL and CASUAL_ONLINE modes; RANKED mode needs networking logic.
     */
    @FXML
    private void handleDrawRequest() {
        if (matchType == GameEnums.MatchType.LOCAL || matchType == GameEnums.MatchType.CASUAL_ONLINE) {
            showGameOverDialog("Draw requested and accepted."); // Auto-accept for simplicity.
        }
        // TODO: Add networking logic for RANKED mode to send draw request to opponent.
    }

    /**
     * Handles the forfeit action when the forfeit button is clicked.
     * Shows a confirmation dialog and ends the game if confirmed.
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
                String forfeitingPlayer = game.getActivePlayer().equals(game.Player1) ? player1Name : player2Name;
                String message = forfeitingPlayer + " has forfeited the game.";

                if (matchType == GameEnums.MatchType.RANKED) {
                    // TODO: Elo decrease logic
                }

                showGameOverDialog(message);
            } else if (timer != null) {
                timer.play();
            }
        });
    }

    /**
     * Handles sending a chat message.
     * Creates an HBox containing the active player's avatar and message text,
     * then adds it to the chatMessagesBox. (Networking integration can be added later.)
     */
    @FXML
    private void handleSendChat() {
        // Retrieve the text from the chat input field.
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            // Create an HBox with spacing of 8
            HBox chatMessageBox = new HBox();
            chatMessageBox.setSpacing(8.0);

            // Create the avatar ImageView and set its dimensions.
            ImageView avatarView = new ImageView();
            // Use a different avatar image based on which player is active.
            if (game.getActivePlayer().equals(game.Player1)) {
                avatarView.setImage(new Image(getClass().getResourceAsStream("/game_images/player1-in game chat.png")));
            } else {
                avatarView.setImage(new Image(getClass().getResourceAsStream("/game_images/player2-in game chat.png")));
            }
            avatarView.setFitHeight(32);
            avatarView.setFitWidth(32);

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
     * Handles the game over state (win or draw) and shows a dialog with options.
     */
    private void handleGameOver() {
        if (timer != null) timer.stop(); // Stop the timer.
        String winner = game.getWinner();
        String message;
        if (game.getGameState() == 3) {
            message = "Game ended in a draw!";
            // Re-enable draw button for the next game.
            requestDrawButton.setDisable(false);
            requestDrawButton.setStyle("-fx-background-color: #a3a3a3; -fx-text-fill: #000000;");
        } else if (!winner.isEmpty()) {
            String winningPlayer = winner.equals(game.Player1) ? player1Name : player2Name;
            message = winningPlayer + " wins!";
        } else {
            message = "Game ended unexpectedly.";
        }
        showGameOverDialog(message);
    }

    /**
     * Shows a game over dialog with options to play again or return to the main menu.
     * @param message The message to display (e.g., "Player 1 wins!").
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
     * Resets the game state for a new game without closing the window.
     */
    private void resetGame() {
        game.startNewGame(player1Name, player2Name); // Reset the game logic (board, active player, etc.).
        // Clear the board visually.
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                cellCircles[row][col].setFill(Color.LIGHTGRAY);
            }
        }
        // Reset timers and UI elements.
        player1TimeLabel.setText("00:15");
        player2TimeLabel.setText("00:15");
        requestDrawButton.setDisable(matchType == GameEnums.MatchType.LOCAL);
        requestDrawButton.setStyle(matchType == GameEnums.MatchType.LOCAL ?
                "-fx-background-color: #262626; -fx-text-fill: #e5e7eb;" :
                "-fx-background-color: #a3a3a3; -fx-text-fill: #000000;");
        updateTurnIndicators();
        startTimerForActivePlayer();
    }

    /**
     * Navigates back to the main game menu (GameMenu_2) when the user chooses to exit the game.
     * Since there’s no MainDashboard.fxml, this method creates a new GameMenu_2 instance and
     * sets its scene on the current stage, replacing the Connect Four game screen.
     */
    private void goBackToMainMenu() {
        try {
            // Create a new instance of GameMenu_2 to generate the main menu scene.
            GameMenu_2 gameMenu = new GameMenu_2();

            // Get the current stage (window) from the gameBoardPane’s scene.
            Stage stage = (Stage) gameBoardPane.getScene().getWindow();

            // Create the main menu scene using GameMenu_2’s createScene method.
            // Pass the current stage to reuse the existing window.
            Scene menuScene = gameMenu.createScene(stage);

            // Set the new scene on the stage, replacing the Connect Four game screen.
            stage.setScene(menuScene);

            // Update the stage title to reflect the main menu (optional).
            stage.setTitle("OMG Platform - Game Menu");

            // Show the updated stage with the main menu.
            stage.show();
        } catch (Exception e) {
            // Handle any unexpected errors during scene creation or transition.
            e.printStackTrace();
            // If something goes wrong, close the current game window as a fallback.
            Stage stage = (Stage) gameBoardPane.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleNetworkDetailsClick() {
        // Get network details from your NetworkUtils class.
        String networkInfo = NetworkUtils.getNetworkDetails();

        // Create an information alert to display the network information.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Network Details");
        alert.setHeaderText("Your Network Information");
        alert.setContentText(networkInfo);

        // Show the dialog and wait for the user to close it.
        alert.showAndWait();
    }
}