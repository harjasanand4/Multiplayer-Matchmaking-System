package com.game.gui;

import com.game.gamelogic.TicTacToeLogic;
import com.game.gamelogic.TicTacToePiece;
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
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TicTacToe {
    private TicTacToeLogic game; // Replace TicTacToeGameStub with TicTacToeLogic
    private Pane[][] cellPanes;
    private Label statusLabel;
    private Image xImage;
    private Image oImage;
    private Stage primaryStage;
    private Stage gameStage;
    private Label moveCountLabel;
    private Label timerLabel;
    private VBox chatMessagesBox;

    // Constructor
    public TicTacToe(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameStage = new Stage();

        // Initialize game logic with two players (e.g., "X" and "O")
        game = new TicTacToeLogic("X", "O"); // PlayerX and PlayerO
        game.startNewGame(); // Start the game

        // Load images
        try {
            xImage = new Image(getClass().getResourceAsStream("tic_tac_toe_assets/x_icon.png"));
            oImage = new Image(getClass().getResourceAsStream("tic_tac_toe_assets/o_icon.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            try {
                xImage = new Image(getClass().getResource("/tic_tac_toe_assets/x_icon.png").toExternalForm());
                oImage = new Image(getClass().getResource("/tic_tac_toe_assets/o_icon.png").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Fallback image loading failed: " + ex.getMessage());
            }
        }
    }

    // Show the game area
    public void showLobby() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #171717;");

        HBox topBar = createTopBar();
        root.setTop(topBar);

        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(10));
        mainContent.setAlignment(Pos.CENTER); // Center the sidebar and game area

        BorderPane gameAreaPane = createGameBoardArea();
        VBox sidebar = createRightSidebar(); // Renamed from createRightSidebar

        // Add a Region on the right to balance the layout
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();

        rightSpacer.setPrefWidth(100); // Adjust this to control left margin
        HBox.setHgrow(rightSpacer, Priority.NEVER); // Fixed width for right spacer
        HBox.setHgrow(rightSpacer, Priority.ALWAYS); // Right spacer takes remaining space
        HBox.setHgrow(gameAreaPane, Priority.SOMETIMES); // Game area grows less aggressively
        HBox.setHgrow(sidebar, Priority.NEVER); // Sidebar stays fixed width

        // Place sidebar on the left, followed by game area and right spacer
        mainContent.getChildren().addAll(leftSpacer, sidebar, gameAreaPane, rightSpacer);

        root.setCenter(mainContent);
        BorderPane.setAlignment(mainContent, Pos.CENTER); // Center the HBox in the root

        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 1200, 900); // Larger initial size for bigger board
        gameStage.setTitle("Tic Tac Toe");
        gameStage.setScene(scene);
        Main.StageManager.configureStage(gameStage);
        gameStage.setResizable(false);
        gameStage.show();

        primaryStage.hide();

        startGameTimer();
    }

    private HBox createTopBar() {
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

    private BorderPane createGameBoardArea() {
        BorderPane gameArea = new BorderPane();
        gameArea.setPrefWidth(900); // Match the board width
        gameArea.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");
        gameArea.setPadding(new Insets(10));

        // Status label at top
        statusLabel = new Label("Player " + game.getActivePlayer() + "'s turn");
        statusLabel.setFont(Font.font("Arial", 20)); // Slightly larger font
        statusLabel.setTextFill(Color.WHITE);
        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER);

        // Game board
        Pane boardContainer = createBoard();
        boardContainer.setStyle("-fx-background-color: #252525;");
        StackPane boardWrapper = new StackPane(boardContainer);
        boardWrapper.setAlignment(Pos.CENTER);
        gameArea.setCenter(boardWrapper);

        // Bottom info bar
        HBox infoBar = new HBox(20);
        infoBar.setAlignment(Pos.CENTER_LEFT);

        moveCountLabel = new Label("Move: " + game.getTurnCounter());
        moveCountLabel.setTextFill(Color.WHITE);
        moveCountLabel.setFont(Font.font("Arial", 16));

        Region infoSpacer = new Region();
        HBox.setHgrow(infoSpacer, Priority.ALWAYS);

        timerLabel = new Label("Time: 00:00");
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setFont(Font.font("Arial", 16));

        infoBar.getChildren().addAll(moveCountLabel, infoSpacer, timerLabel);

        // Add components to game area
        gameArea.setTop(statusBox);
        gameArea.setCenter(boardContainer);
        gameArea.setBottom(infoBar);
        System.out.println("Board size: " + boardContainer.getPrefWidth() + "x" + boardContainer.getPrefHeight());

        return gameArea;
    }

    private VBox createRightSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(400); // Increase width to make chat broader (was 300)
        sidebar.setMaxWidth(400); // Prevent stretching beyond 400px
        sidebar.setMinWidth(400); // Ensure it doesn't shrink below 400px

        VBox gameInfoCard = new VBox(5);
        gameInfoCard.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");
        gameInfoCard.setPadding(new Insets(10));

        Label gameTitle = new Label("Tic Tac Toe");
        gameTitle.setFont(Font.font("Arial", 16));
        gameTitle.setTextFill(Color.WHITE);

        HBox playersInfo = new HBox(10);
        playersInfo.setAlignment(Pos.CENTER);

        Label player1 = new Label("Player X");
        player1.setTextFill(Color.WHITE);

        Label vsLabel = new Label("vs");
        vsLabel.setTextFill(Color.WHITE);

        Label player2 = new Label("Player O");
        player2.setTextFill(Color.WHITE);

        playersInfo.getChildren().addAll(player1, vsLabel, player2);
        gameInfoCard.getChildren().addAll(gameTitle, playersInfo);

        VBox chatCard = new VBox(5);
        chatCard.setStyle("-fx-background-color: #252525; -fx-background-radius: 10;");
        chatCard.setPadding(new Insets(10));
        chatCard.setPrefHeight(300);
        VBox.setVgrow(chatCard, Priority.ALWAYS); // Ensure chat grows vertically

        Label chatLabel = new Label("Chat");
        chatLabel.setTextFill(Color.WHITE);

        chatMessagesBox = new VBox(5);
        ScrollPane chatScrollPane = new ScrollPane(chatMessagesBox);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle("-fx-background: #252525; -fx-background-color: #333333;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS); // Ensure scroll pane grows

        addChatMessage("Player X", "Good game!");
        addChatMessage("Player O", "Thanks, you too!");

        HBox messageInputBox = new HBox(5);
        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        HBox.setHgrow(messageField, Priority.ALWAYS); // Ensure text field grows horizontally

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
        button.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
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

    private Pane createBoard() {
        Pane boardContainer = new Pane();
        boardContainer.setPrefSize(900, 900); // Much larger board size
        boardContainer.setStyle("-fx-background-color: #252525;");


        // Adjust grid lines for the larger size
        Line hLine1 = new Line(15, 315, 885, 315); // Adjusted for 900 width/height
        Line hLine2 = new Line(15, 615, 885, 615);
        hLine1.setStrokeWidth(10); // Thicker lines for visibility
        hLine2.setStrokeWidth(10);
        hLine1.setStroke(Color.WHITE);
        hLine2.setStroke(Color.WHITE);

        Line vLine1 = new Line(315, 15, 315, 885);
        Line vLine2 = new Line(615, 15, 615, 885);
        vLine1.setStrokeWidth(10);
        vLine2.setStrokeWidth(10);
        vLine1.setStroke(Color.WHITE);
        vLine2.setStroke(Color.WHITE);

        boardContainer.getChildren().addAll(hLine1, hLine2, vLine1, vLine2);

        cellPanes = new Pane[3][3];

        // Adjust cell size and position
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = col * 300 + 15; // 300px per cell, plus padding
                int y = row * 300 + 15;

                Pane cell = new Pane();
                cell.setLayoutX(x);
                cell.setLayoutY(y);
                cell.setPrefSize(300, 300); // Much larger cells

                final int finalRow = row;
                final int finalCol = col;
                cell.setOnMouseClicked(e -> handleCellClick(finalRow, finalCol));

                cellPanes[row][col] = cell;
                boardContainer.getChildren().add(cell);
            }
        }

        return boardContainer;
    }

    private void handleCellClick(int row, int col) {
        if (game.isGameOver()) {
            return; // Ignore clicks if game is over
        }

        // Place piece for the active player
        String activePlayer = game.getActivePlayer();
        game.placePiece(activePlayer, row, col);

        // Update the GUI
        updateCellDisplay(row, col);
        //moveCountLabel.setText("Move: " + game.getTurnCounter());
        updateGameStatus();
    }

    private void updateCellDisplay(int row, int col) {
        Pane cellPane = cellPanes[row][col];
        cellPane.getChildren().clear();

        if (game.getGameBoard() != null) {
            TicTacToePiece piece = (TicTacToePiece) game.getGameBoard().getBoard()[row][col];
            if (piece != null) {
                String player = piece.getColour();
                ImageView imageView = null;
                if (player.equals("X") && xImage != null) {
                    imageView = new ImageView(xImage);
                } else if (player.equals("O") && oImage != null) {
                    imageView = new ImageView(oImage);
                } else {
                    Label symbolLabel = new Label(player);
                    symbolLabel.setFont(Font.font("Arial", 144)); // Much larger font for 300x300 cells
                    symbolLabel.setTextFill(player.equals("X") ? Color.LIGHTBLUE : Color.LIGHTGREEN);
                    symbolLabel.setLayoutX(90); // Centered in 300x300 cell
                    symbolLabel.setLayoutY(60);
                    cellPane.getChildren().add(symbolLabel);
                    return;
                }

                imageView.setFitHeight(240); // Larger image to fit 300x300 cell
                imageView.setFitWidth(240);
                imageView.setLayoutX(30); // Centered in 300x300 cell
                imageView.setLayoutY(30);
                cellPane.getChildren().add(imageView);
            }
        } else {
            System.err.println("Game board is not initialized!");
        }
    }

    private void updateGameStatus() {
        if (game.isGameOver()) {
            String winner = game.getWinner();
            if (winner.equals("Draw")) {
                statusLabel.setText("Game ended in a draw!");
                addChatMessage("System", "Game ended in a draw!");
            } else {
                statusLabel.setText("Player " + winner + " wins!");
                addChatMessage("System", "Player " + winner + " wins!");
            }
        } else {
            statusLabel.setText("Player " + game.getActivePlayer() + "'s turn");
        }
    }

    private void resetGame() {
        game.restartGame();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane cellPane = cellPanes[row][col];
                cellPane.getChildren().clear();
            }
        }

        statusLabel.setText("Player " + game.getActivePlayer() + "'s turn");
        //moveCountLabel.setText("Move: " + game.getTurnCounter());

        addChatMessage("System", "Game restarted!");
    }

    private void startGameTimer() {
        timerLabel.setText("Time: 00:00"); // Placeholder for now
    }

    // Helper method to access the game board (optional, for convenience)
    private TicTacToeLogic getGame() {
        return game;
    }
}