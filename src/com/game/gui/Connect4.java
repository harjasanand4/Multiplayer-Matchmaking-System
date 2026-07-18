package com.game.gui;

import com.game.gamelogic.Connect4Board;
import com.game.gamelogic.Connect4Logic;
import com.game.gamelogic.Connect4Piece;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Connect4 {
    private Connect4Logic game;
    private Circle[][] cellCircles;
    private Label statusLabel;
    private Stage primaryStage;
    private Stage gameStage;
    private Label moveCountLabel;
    private Label timerLabel;
    private VBox chatMessagesBox;
    private Image redPieceImage;
    private Image bluePieceImage;
    private Pane boardContainer;
    private Timeline gameTimer; // Added to manage timer
    private int timeLeft; // Added to track remaining time

    public Connect4(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameStage = new Stage();

        game = new Connect4Logic();
        game.startNewGame("Red", "Blue");

        try {
            redPieceImage = new Image(getClass().getResourceAsStream("/connect4_assets/connect4_images/red_piece.png"));
            bluePieceImage = new Image(getClass().getResourceAsStream("/connect4_assets/connect4_images/blue_piece.png"));
        } catch (Exception e) {
            System.err.println("Error loading piece images: " + e.getMessage());
            redPieceImage = null;
            bluePieceImage = null;
        }
    }

    public void showLobby() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #171717;");

        HBox topBar = createTopBar();
        root.setTop(topBar);

        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(10));

        BorderPane gameAreaPane = createGameBoardArea();
        VBox rightSidebar = createRightSidebar();

        mainContent.getChildren().addAll(gameAreaPane, rightSidebar);
        HBox.setHgrow(gameAreaPane, Priority.ALWAYS);

        root.setCenter(mainContent);

        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 950, 620);
        gameStage.setTitle("Connect 4");
        gameStage.setScene(scene);
        gameStage.setResizable(false);
        gameStage.show();

        primaryStage.hide();

        startGameTimer();
    }

    private BorderPane createGameBoardArea() {
        BorderPane gameArea = new BorderPane();
        gameArea.setPrefWidth(600);
        gameArea.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");
        gameArea.setPadding(new Insets(10));

        // Status label
        statusLabel = new Label("Player " + game.getActivePlayer() + "'s turn");
        statusLabel.setFont(Font.font("Arial", 16));
        statusLabel.setTextFill(Color.WHITE);
        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER);

        // Create game board
        boardContainer = createBoard();
        boardContainer.setStyle("-fx-background-color: #252525;");

        // Bottom info bar
        HBox infoBar = new HBox(20);
        infoBar.setAlignment(Pos.CENTER_LEFT);

        moveCountLabel = new Label("Move: " + game.getTurnCounter());
        moveCountLabel.setTextFill(Color.WHITE);

        Region infoSpacer = new Region();
        HBox.setHgrow(infoSpacer, Priority.ALWAYS);

        timerLabel = new Label("Time: 00:30"); // Initial time set to 30 seconds
        timerLabel.setTextFill(Color.WHITE);

        infoBar.getChildren().addAll(moveCountLabel, infoSpacer, timerLabel);

        // Add components to game area
        gameArea.setTop(statusBox);
        gameArea.setCenter(boardContainer);
        gameArea.setBottom(infoBar);

        return gameArea;
    }

    private Pane createBoard() {
        Pane boardContainer = new Pane();
        boardContainer.setPrefSize(600, 520);
        boardContainer.setStyle("-fx-background-color: #1E40AF;");

        // Calculate total board width and height
        int circleRadius = 40;
        int horizontalSpacing = 75;
        int verticalSpacing = 75;

        // Total board dimensions
        int boardWidth = horizontalSpacing * 6 + circleRadius * 2;
        int boardHeight = verticalSpacing * 5 + circleRadius * 2;

        // Calculate starting x and y to center the board
        int startX = (int)((600 - boardWidth) / 2);
        int startY = (int)((520 - boardHeight) / 2);

        cellCircles = new Circle[6][7];

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int x = startX + col * horizontalSpacing + circleRadius;
                int y = startY + row * verticalSpacing + circleRadius;

                Circle cell = new Circle(x, y, circleRadius);
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.LIGHTGRAY);
                cell.setStrokeWidth(2);

                final int finalCol = col;
                cell.setOnMouseClicked(e -> handleColumnClick(finalCol));

                cellCircles[row][col] = cell;
                boardContainer.getChildren().add(cell);
            }
        }

        return boardContainer;
    }

    private void handleColumnClick(int column) {
        if (game.getActivePlayer() == null) {
            return; // Game is over
        }

        game.placePiece(game.getActivePlayer(), column);
        updateBoardDisplay();
        updateGameStatus();
        if (gameTimer != null) {
            gameTimer.stop();
            startGameTimer(); // Restart timer after a valid move
        }
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
                    cell.setFill("red".equals(piece.getColour()) ? Color.RED : Color.BLACK);
                } else {
                    cell.setFill(Color.LIGHTGRAY);
                }
            }
        }
    }

    private void updateGameStatus() {
        if (game.getActivePlayer() == null || game.getActivePlayer().equals("None")) {
            String winner = game.getWinner();
            statusLabel.setText(winner.equals("Draw") ? "Game ended in a draw!" : "Player " + winner + " wins!");
            addChatMessage("System", statusLabel.getText());
            if (gameTimer != null) gameTimer.stop();
        } else {
            statusLabel.setText("Player " + game.getActivePlayer() + "'s turn");
        }
        moveCountLabel.setText("Move: " + game.getTurnCounter());
    }

    // Reuse methods from TicTacToe implementation
    private HBox createTopBar() {
        // Similar implementation to TicTacToe
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #000000;");

        Label gameLogo = new Label("OMG Platform");
        gameLogo.setFont(Font.font("Arial", 16));
        gameLogo.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button statsButton = createIconButton("Stats");
        Button achievementsButton = createIconButton("Achievements");
        Button settingsButton = createIconButton("Settings");
        Button notificationsButton = createIconButton("Notifications");
        Button profileButton = createIconButton("Profile");

        topBar.getChildren().addAll(gameLogo, spacer, statsButton, achievementsButton,
                settingsButton, notificationsButton, profileButton);

        return topBar;
    }

    private VBox createRightSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(300);

        // Game Info Card
        VBox gameInfoCard = new VBox(5);
        gameInfoCard.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");
        gameInfoCard.setPadding(new Insets(10));

        Label gameTitle = new Label("Connect 4");
        gameTitle.setFont(Font.font("Arial", 16));
        gameTitle.setTextFill(Color.WHITE);

        HBox playersInfo = new HBox(10);
        playersInfo.setAlignment(Pos.CENTER);

        Label player1 = new Label("Player Red");
        player1.setTextFill(Color.WHITE);

        Label vsLabel = new Label("vs");
        vsLabel.setTextFill(Color.WHITE);

        Label player2 = new Label("Player Blue");
        player2.setTextFill(Color.WHITE);

        playersInfo.getChildren().addAll(player1, vsLabel, player2);
        gameInfoCard.getChildren().addAll(gameTitle, playersInfo);

        // Chat Card (similar to TicTacToe)
        VBox chatCard = new VBox(5);
        chatCard.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");
        chatCard.setPadding(new Insets(10));
        chatCard.setPrefHeight(300);
        VBox.setVgrow(chatCard, Priority.ALWAYS);

        Label chatLabel = new Label("Chat");
        chatLabel.setTextFill(Color.WHITE);

        chatMessagesBox = new VBox(5);
        ScrollPane chatScrollPane = new ScrollPane(chatMessagesBox);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle("-fx-background: #252525; -fx-background-color: #333333;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        addChatMessage("Player Red", "Good game!");
        addChatMessage("Player Blue", "Thanks, you too!");

        HBox messageInputBox = new HBox(5);
        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle("-fx-background-color: #333333; -fx-text-fill: #494848;");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = new Button("➤");
        sendButton.setStyle("-fx-background-color: #4682B4;");
        sendButton.setTextFill(Color.WHITE);
        sendButton.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                addChatMessage("Player " + game.getActivePlayer(), message);
                messageField.clear();
            }
        });

        messageInputBox.getChildren().addAll(messageField, sendButton);

        chatCard.getChildren().addAll(chatLabel, chatScrollPane, messageInputBox);

        // Game Control Buttons
        Button restartButton = createControlButton("⟳ Restart Game");
        Button surrenderButton = createControlButton("🏳 Surrender");
        Button leaveButton = createControlButton("⬅️ Leave Game");

        restartButton.setOnAction(e -> resetGame());
        leaveButton.setOnAction(e -> {
            gameStage.close();
            primaryStage.show();
        });

        sidebar.getChildren().addAll(gameInfoCard, chatCard, restartButton, surrenderButton, leaveButton);

        return sidebar;
    }

    private void resetGame() {
        game.startNewGame("red", "blue");

        // Reset board display
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                // Clear any ImageViews and restore original circles
                if (!(boardContainer.getChildren().get(row * 7 + col) instanceof Circle)) {
                    boardContainer.getChildren().set(row * 7 + col, cellCircles[row][col]);
                }
                cellCircles[row][col].setFill(Color.LIGHTGRAY);
            }
        }

        statusLabel.setText("Player " + game.getActivePlayer() + "'s turn");
        moveCountLabel.setText("Move: " + game.getTurnCounter());

        addChatMessage("System", "Game restarted!");
        if (gameTimer != null) {
            gameTimer.stop();
        }
        startGameTimer();
    }

    private void startGameTimer() {
        if (gameTimer != null) gameTimer.stop();
        timeLeft = 30; // Set to 30 seconds per turn, similar to TicTacToeLogic
        timerLabel.setText(String.format("Time: %02d", timeLeft));

        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            if (timeLeft >= 0) {
                timerLabel.setText(String.format("Time: %02d", timeLeft));
            }
            if (timeLeft < 0) {
                // Handle timeout with switchPlayer logic
                String currentPlayer = game.getActivePlayer();
                if (currentPlayer != null && !currentPlayer.equals("None")) {
                    if (currentPlayer.equals("Red")) {
                        game.placePiece("Blue", -1); // Invalid column to skip placement
                    } else if (currentPlayer.equals("Blue")) {
                        game.placePiece("Red", -1); // Switch to Red without placing
                    }
                    updateGameStatus();
                    addChatMessage("System", "Time expired for Player " + currentPlayer + ". Turn switched.");
                    timeLeft = 30; // Reset timer for new player
                    timerLabel.setText(String.format("Time: %02d", timeLeft));
                }
            }
            if (game.getActivePlayer() == null || game.getActivePlayer().equals("None")) {
                gameTimer.stop();
                updateGameStatus();
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(5, 10, 5, 10));
        footer.setStyle("-fx-background-color: #000000;");

        Label copyrightLabel = new Label("© 2025 OMG Platform. All rights reserved.");
        copyrightLabel.setTextFill(Color.GRAY);
        copyrightLabel.setFont(Font.font("Arial", 10));

        footer.getChildren().add(copyrightLabel);

        return footer;
    }

    private Button createIconButton(String tooltip) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent;");
        button.setPrefSize(30, 30);
        button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        return button;
    }

    private Button createControlButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPadding(new Insets(10));
        button.setStyle("-fx-background-color: #333333; -fx-text-fill: #716e6e;");
        return button;
    }

    private void addChatMessage(String sender, String message) {
        HBox messageBox = new HBox(5);

        Label senderLabel = new Label(sender + ":");
        senderLabel.setTextFill(Color.LIGHTBLUE);

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);

        messageBox.getChildren().addAll(senderLabel, messageLabel);
        chatMessagesBox.getChildren().add(messageBox);
    }
}