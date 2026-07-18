package com.game.gui;

import javafx.application.Platform;
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

/**
 * CheckersController manages the Checkers game UI.
 *
 * Responsibilities include:
 *   - Initializing the game board and setting up the UI components.
 *   - Responding to user actions (e.g., moving pieces, draw requests, forfeits, and chat messages).
 *   - Updating UI elements (e.g., turn labels, timers, captured pieces) in real time.
 *
 * Networking Integration Note:
 *   While this implementation is local, the networking team can study the methods (e.g., handleDrawRequest,
 *   handleForfeit) to determine where network messages might be sent/received in a multi-player context.
 *   For example, instead of directly showing an Alert on a draw request, a networked version might send a draw
 *   proposal message to a remote opponent and then await their response.
 */
public class CheckersController {

    // ----- UI Components (Injected from FXML) -----
    // Player 1 UI elements:
    @FXML private Label player1NameLabel;
    @FXML private Label player1RankLabel;
    @FXML private Label player1TimeLabel;
    @FXML private VBox player1CapturedPieces;
    @FXML private Rectangle player1TurnIndicator;
    @FXML private Label player1TurnLabel;

    // Player 2 UI elements:
    @FXML private Label player2NameLabel;
    @FXML private Label player2RankLabel;
    @FXML private Label player2TimeLabel;
    @FXML private VBox player2CapturedPieces;
    @FXML private Rectangle player2TurnIndicator;
    @FXML private Label player2TurnLabel;

    // Piece icons for each player.
    @FXML private ImageView player1PieceIcon;
    @FXML private ImageView player2PieceIcon;

    // Game board container and additional UI elements (timer display, draw status, control buttons).
    @FXML private Pane boardPane;
    @FXML private Label moveCountLabel;
    @FXML private Label drawStatusLabel;
    @FXML private Button requestDrawButton;
    @FXML private Button forfeitButton;

    // Chat UI components.
    @FXML private TextField chatInputField;
    @FXML private Button sendChatButton;
    @FXML private VBox chatMessagesBox;

    @FXML private Label networkTextLabel;
    @FXML private ImageView networkIcon;

    // Game title label (e.g., "Checkers - Casual")
    @FXML private Label gameTitleLabel;

    // Chat avatars to be displayed next to chat messages.
    private Image player1ChatAvatar;
    private Image player2ChatAvatar;

    // Flags for draw requests (could be used for multi-client logic).
    private boolean player1RequestedDraw = false;
    private boolean player2RequestedDraw = false;

    // Reference to the main Checkers game class, which creates the game board.
    private Checkers game;
    // The Board instance that contains the game logic and handles moves.
    private Checkers.Board board;

    // Default player names.
    String player1Name = "Player 1";
    String player2Name = "Player 2";

    // The match mode (LOCAL, CASUAL_ONLINE, RANKED).
    private GameEnums.MatchType matchType = GameEnums.MatchType.LOCAL;

    /**
     * Sets the Checkers game instance.
     * This allows the controller to call methods on the game (e.g., resetting the game).
     *
     * @param game the main Checkers application instance.
     */
    public void setGame(Checkers game) {
        this.game = game;
    }

    /**
     * startGame() is called when the Checkers screen is loaded.
     * It creates a new Board (which holds all game logic),
     * adds it to the gameBoardPane, initializes the UI labels, and starts the game.
     * The method also sets up dynamic image assignments and chat input handlers.
     */
    public void startGame() {
        // Create a new Board instance linked to this controller.
        board = game.new Board(this);
        board.setLayoutX(0.0);
        board.setLayoutY(0.0);
        board.prefWidthProperty().bind(boardPane.widthProperty());
        board.prefHeightProperty().bind(boardPane.heightProperty());
        boardPane.getChildren().add(board);

        // Set player names and ranks on the UI.
        player1NameLabel.setText(player1Name);
        player1RankLabel.setText("Rank: 1205");
        player2NameLabel.setText(player2Name);
        player2RankLabel.setText("Rank: 1275");

        // Initialize the captured pieces count display.
        updateCapturedCounts(0, 0);

        // Dynamically load and assign piece icon images.
        Image player1Icon = new Image(getClass().getResourceAsStream("/checkers_assets/checkers_images/moo_cow.png"));
        Image player2Icon = new Image(getClass().getResourceAsStream("/checkers_assets/checkers_images/chicken.png"));
        player1PieceIcon.setImage(player1Icon);
        player2PieceIcon.setImage(player2Icon);

        // Setup the draw request button appearance and disable it initially.
        requestDrawButton.setDisable(true);
        requestDrawButton.setStyle("-fx-background-color: #262626; -fx-text-fill: #404040; -fx-font-size: 19px; -fx-font-family: 'Inter'; -fx-border-radius: 10; -fx-background-radius: 10;");
        if (requestDrawButton.getGraphic() instanceof ImageView) {
            ((ImageView) requestDrawButton.getGraphic()).setOpacity(0.5);
        }

        // Initialize draw status label.
        drawStatusLabel.setText("");
        drawStatusLabel.setVisible(false);

        // Start the game logic on the Board.
        board.startGame();
        updateUIToMatchType();

        // Load chat avatars.
        try {
            player1ChatAvatar = new Image(getClass().getResourceAsStream("/game_images/player1-avatar.png"));
            player2ChatAvatar = new Image(getClass().getResourceAsStream("/game_images/player2-avatar.png"));
        } catch (Exception e) {
            System.err.println("Error loading chat avatars: " + e.getMessage());
        }

        // Set up chat input handlers (send chat on button click or pressing Enter).
        sendChatButton.setOnAction(e -> handleSendChat());
        chatInputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSendChat();
                e.consume();
            }
        });
    }

    /**
     * Sets the match type for the current game.
     * In networked modes, this could trigger different UI behavior.
     *
     * @param matchType the match mode (LOCAL, CASUAL_ONLINE, RANKED).
     */
    public void setMatchType(GameEnums.MatchType matchType) {
        this.matchType = matchType;
        if (gameTitleLabel != null) {
            updateUIToMatchType();
        }
    }

    /**
     * Updates various UI elements (game title, draw button style, etc.)
     * based on the current match type.
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
                gameTitleLabel.setText("Checkers - Casual Match");
                requestDrawButton.setDisable(true);
                requestDrawButton.setStyle("-fx-background-color: #262626; -fx-text-fill: #e5e7eb; -fx-border-radius: 13; -fx-background-radius: 13;");
                break;
            case CASUAL_ONLINE:
            case RANKED:
                gameTitleLabel.setText(matchType == GameEnums.MatchType.RANKED ? "Checkers - Ranked Match" : "Checkers - Casual Match");
                // Enable draw if enough non-capture moves have occurred.
                requestDrawButton.setDisable(board.getNonCaptureMoves() < 20);
                requestDrawButton.setStyle(
                        board.getNonCaptureMoves() >= 20 ?
                                "-fx-background-color: #a3a3a3; -fx-text-fill: #000000; -fx-border-radius: 13; -fx-background-radius: 13;" :
                                "-fx-background-color: #262626; -fx-text-fill: #e5e7eb; -fx-border-radius: 13; -fx-background-radius: 13;"
                );
                break;
        }
    }

    /**
     * Sets new player names and resets the game with those names.
     * This method is useful when players change their names or start a new game.
     *
     * @param p1Name Name for Player 1.
     * @param p2Name Name for Player 2.
     */
    public void setPlayerNames(String p1Name, String p2Name) {
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        player1NameLabel.setText(p1Name);
        player2NameLabel.setText(p2Name);
        player1TurnLabel.setText(p1Name + "’s Turn");
        player2TurnLabel.setText(p2Name + "’s Turn");
        // Reset the game with new names.
        board.resetGame(p1Name, p2Name);
        updateUIToMatchType();
    }

    /**
     * handleDrawRequest() is called when a user selects "Request Draw".
     * If in LOCAL mode, it simply ends the game as a draw.
     * In online modes, it simulates sending the draw proposal to the opponent,
     * then handles the accepted draw after a short delay.
     */
    @FXML
    private void handleDrawRequest() {
        if (matchType == GameEnums.MatchType.LOCAL) {
            board.announceDraw();
            showDrawPopup();
        } else if (matchType == GameEnums.MatchType.RANKED) {
            // For a Ranked match, ensure the button is enabled after 20 non-capturing moves
            drawStatusLabel.setText("Draw Proposed - Waiting for Opponent");
            drawStatusLabel.setVisible(true);

            // Use a separate thread to simulate a delay of 4 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(4000); // wait 4 seconds before simulating opponent response
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    drawStatusLabel.setVisible(false);
                    // Show a dialog confirming the opponent accepted the draw request
                    Alert drawAcceptedAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    drawAcceptedAlert.setTitle("Draw Accepted");
                    drawAcceptedAlert.setHeaderText("Opponent Accepted Draw Request");
                    drawAcceptedAlert.setContentText("Both players have been awarded 4 Elo points.");
                    ButtonType rematchButton = new ButtonType("Request Rematch");
                    ButtonType menuButton = new ButtonType("Back to Main Menu");
                    drawAcceptedAlert.getButtonTypes().setAll(rematchButton, menuButton);
                    drawAcceptedAlert.showAndWait().ifPresent(result -> {
                        if (result == rematchButton) {
                            // Rematch sequence: simulate a 4-second delay then reset board.
                            drawStatusLabel.setText("Rematch Requested - Waiting for Opponent");
                            drawStatusLabel.setVisible(true);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(4000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                Platform.runLater(() -> {
                                    drawStatusLabel.setVisible(false);
                                    board.resetGame(player1Name, player2Name);
                                });
                            }).start();
                        } else {
                            // Navigate back to the main menu.
                            goBackToMainMenu();
                        }
                    });
                });
            }).start();
        } else { // for CASUAL_ONLINE if any similar logic is desired
            // your existing logic or similar adjustments can be done here
        }
    }

    /**
     * Called after a draw request is accepted.
     * Updates the UI to show that the game ended in a draw.
     */
    private void handleDrawAccepted() {
        drawStatusLabel.setVisible(false);
        String message = "Game ended in a draw!";
        if (matchType == GameEnums.MatchType.RANKED) {
            message += " Elo rating updated.";
        }
        showGameOverDialog(message, matchType == GameEnums.MatchType.CASUAL_ONLINE || matchType == GameEnums.MatchType.RANKED);
    }

    /**
     * Displays a popup dialog when a draw is proposed.
     * The user can choose to play again or return to the main menu.
     */
    private void showDrawPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Draw Requested");
        alert.setHeaderText("The game has ended in a draw.");
        alert.setContentText("What would you like to do?");

        ButtonType playAgain = new ButtonType("Play Again");
        ButtonType backToMenu = new ButtonType("Back to Main Menu");
        alert.getButtonTypes().setAll(playAgain, backToMenu);

        alert.showAndWait().ifPresent(result -> {
            if (result == playAgain) {
                resetGame();
            } else {
                goBackToMainMenu();
            }
        });
    }

    /**
     * updateTimerLabels() updates the UI labels that display the remaining time for each player.
     *
     * @param player1Time remaining time for Player 1 (in seconds)
     * @param player2Time remaining time for Player 2 (in seconds)
     */
    public void updateTimerLabels(int player1Time, int player2Time) {
        player1TimeLabel.setText(formatTime(player1Time));
        player2TimeLabel.setText(formatTime(player2Time));
    }

    /**
     * updateCapturedCounts() updates the UI portions that display how many pieces have been captured.
     *
     * @param player1Captured number of pieces captured by Player 1.
     * @param player2Captured number of pieces captured by Player 2.
     */
    public void updateCapturedCounts(int player1Captured, int player2Captured) {
        updateCapturedPieces(player1CapturedPieces, player1Captured);
        updateCapturedPieces(player2CapturedPieces, player2Captured);
    }

    /**
     * updateCapturedPieces() updates a given VBox with circles representing captured pieces.
     *
     * @param capturedBox the VBox used to display captured pieces.
     * @param count the number of pieces captured.
     */
    private void updateCapturedPieces(VBox capturedBox, int count) {
        capturedBox.getChildren().clear();
        Label titleLabel = new Label("Captured Pieces");
        titleLabel.setStyle("-fx-text-fill: #e5e7eb; -fx-font-size: 14px; -fx-font-family: 'Inter';");
        capturedBox.getChildren().add(titleLabel);

        HBox lightPieces = new HBox(8);
        HBox darkPieces = new HBox(8);
        for (int i = 0; i < 12; i++) {
            Circle circle = new Circle(8, i < count ? Color.web("#d4d4d4") : Color.web("#262626"));
            if (i < 6) {
                lightPieces.getChildren().add(circle);
            } else {
                darkPieces.getChildren().add(circle);
            }
        }
        capturedBox.getChildren().addAll(lightPieces, darkPieces);
    }

    /**
     * updateTurnLabel() updates the UI to reflect which player's turn it is.
     *
     * @param currentPlayer the identifier for the current player (1 or 2)
     */
    public void updateTurnLabel(int currentPlayer) {
        if (currentPlayer == 1) {
            player1TurnIndicator.setFill(Color.WHITE);
            player1TurnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            player2TurnIndicator.setFill(Color.web("#262626"));
            player2TurnLabel.setStyle("-fx-text-fill: #262626; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
        } else {
            player2TurnIndicator.setFill(Color.WHITE);
            player2TurnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
            player1TurnIndicator.setFill(Color.web("#262626"));
            player1TurnLabel.setStyle("-fx-text-fill: #262626; -fx-font-size: 14px; -fx-font-family: 'Inter'; -fx-alignment: center;");
        }
    }

    /**
     * showMoveCount() displays the number of moves (without captures) that have occurred.
     * If the move count exceeds a threshold, it may enable the draw request button.
     *
     * @param count the number of non-capture moves.
     */
    public void showMoveCount(int count) {
        moveCountLabel.setText("Moves without Capture: " + count + "/40");
        moveCountLabel.setVisible(true);
        updateUIToMatchType();

        if (count >= 20) {
            requestDrawButton.setDisable(false);
            requestDrawButton.setStyle("-fx-background-color: #262626; -fx-text-fill: #e5e7eb; -fx-font-size: 19px; -fx-font-family: 'Inter'; -fx-border-radius: 10; -fx-background-radius: 10;");
            if (requestDrawButton.getGraphic() instanceof ImageView) {
                ((ImageView) requestDrawButton.getGraphic()).setOpacity(1.0);
            }
        }
    }

    /**
     * hideMoveCount() hides the move count label.
     */
    public void hideMoveCount() {
        moveCountLabel.setVisible(false);
    }

    /**
     * getGameBoardPane() returns the pane containing the game board.
     * The networking team (or others) may use this to get the current stage/scene.
     *
     * @return the Pane containing the game board.
     */
    public Pane getGameBoardPane() {
        return boardPane;
    }

    /**
     * resetGame() clears the current game board and starts a new game.
     */
    public void resetGame() {
        boardPane.getChildren().remove(board); // Remove the existing board.
        startGame(); // Start a fresh game.
    }

    /**
     * formatTime() converts seconds to a MM:SS formatted string.
     *
     * @param seconds the number of seconds remaining.
     * @return a formatted time string.
     */
    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%d:%02d", min, sec);
    }

    /**
     * handleForfeit() is called when a user chooses to forfeit the game.
     * It confirms the action and then stops the game timer and ends the game.
     *
     * Networking Note:
     * For an online game, this would also trigger a network message to inform the opponent.
     */
    @FXML
    private void handleForfeit() {
        board.stopTimer();
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
                String forfeitingPlayer = (board.getCurrentPlayer() == 1) ? player1Name : player2Name;
                board.stopTimer();
                String message = forfeitingPlayer + " has forfeited the game.";
                if (matchType == GameEnums.MatchType.RANKED) {
                    message += " Elo rating decreased.";
                }
                showGameOverDialog(message, false);
            } else {
                board.startTimer();
            }
        });
    }

    /**
     * showGameOverDialog() displays an information dialog when the game ends,
     * offering the user the option to play again or return to the main menu.
     *
     * Networking Note:
     * In a networked implementation, additional logic might be required to handle reconnection, rematch requests, etc.
     *
     * @param message The game-over message (win/lose/draw).
     * @param offerRematch flag to indicate if rematch options should be offered.
     */
    private void showGameOverDialog(String message, boolean offerRematch) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType playAgainButton = offerRematch ? new ButtonType("Request a Rematch") : new ButtonType("Play Again");
        ButtonType backToMenuButton = new ButtonType("Back to Main Menu");
        alert.getButtonTypes().setAll(playAgainButton, backToMenuButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == playAgainButton) {
                if (offerRematch) {
                    drawStatusLabel.setText("Rematch Requested - Waiting for Opponent");
                    drawStatusLabel.setVisible(true);
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        resetGame();
                        drawStatusLabel.setVisible(false);
                    });
                } else {
                    resetGame();
                }
            } else {
                // Navigate back to the main menu screen.
                goBackToMainMenu();
            }
        });
    }

    /**
     * handleSendChat() processes user input for chat messages.
     * It retrieves the text, creates a message box with an avatar and the message,
     * and then appends it to the chatMessagesBox.
     *
     * Networking Note:
     * In a networked game, chat messages would be sent to and received from a server.
     */
    @FXML
    private void handleSendChat() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            HBox chatMessageBox = new HBox(8.0);
            ImageView avatarView = new ImageView();
            avatarView.setFitHeight(32);
            avatarView.setFitWidth(32);
            // Determine which player's avatar to use based on the active player.
            avatarView.setImage((board.getCurrentPlayer() == 1) ? player1ChatAvatar : player2ChatAvatar);

            VBox textVBox = new VBox();
            Label nameLabel = new Label((board.getCurrentPlayer() == 1) ? player1Name : player2Name);
            nameLabel.setStyle("-fx-text-fill: #a3a3a3; -fx-font-size: 14px; -fx-font-family: 'Inter';");
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-family: 'Inter';");
            textVBox.getChildren().addAll(nameLabel, messageLabel);

            chatMessageBox.getChildren().addAll(avatarView, textVBox);
            chatMessagesBox.getChildren().add(chatMessageBox);
            chatInputField.clear();
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

    /**
     * goBackToMainMenu() navigates the user from the game screen back to the main game menu (GameMenu_2).
     * It does so by obtaining a new scene from GameMenu_2 and setting it on the current stage.
     *
     * Networking Note:
     * When integrating with a server, returning to the main menu might also require disconnecting from a game session.
     */
    private void goBackToMainMenu() {
        try {
            GameMenu_2 gameMenu = new GameMenu_2();
            Stage stage = (Stage) boardPane.getScene().getWindow();
            Scene menuScene = gameMenu.createScene(stage);
            stage.setScene(menuScene);
            stage.setTitle("OMG Platform - Game Menu");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) boardPane.getScene().getWindow();
            stage.close();
        }
    }
}