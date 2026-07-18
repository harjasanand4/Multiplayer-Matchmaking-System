package com.game.gui;

import com.game.auth.AuthManager;
import com.game.auth.User;
import com.game.auth.session.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * GameMenu_2 class creates the main game selection menu for the OMG Platform.
 * It displays game cards for Tic Tac Toe, Checkers, and Connect 4, allowing users to choose a game
 * and select a match type (Local, Casual Online, or Ranked) via a dialog.
 */
public class GameMenu_2 {

    // Field to store the login scene for potential back navigation (currently unused).
    private Scene loginScene;

    /**
     * Creates the main scene for the game menu.
     * @param stage The primary stage where this scene will be displayed.
     * @return A Scene object containing the full menu layout.
     */
    public Scene createScene(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");
        root.setTop(UIUtils.createTopBar(stage));
        root.setCenter(createCenterContent(stage));
        root.setBottom(UIUtils.createFooter());
        StackPane stackRoot = new StackPane(root);
        return new Scene(stackRoot, 1000, 700);
    }

    /**
     * Builds the central content of the menu, including welcome text and game cards.
     * @param stage The stage for event handling and navigation.
     * @return A VBox containing the welcome message, subheading, and game cards.
     */
    private VBox createCenterContent(Stage stage) {
        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.TOP_LEFT);
        centerBox.setPadding(new Insets(40, 30, 30, 30));

        // Get current user info
        String currentUsername = SessionManager.getInstance().getCurrentUsername();
        User currentUser = AuthManager.getInstance().getDatabase().getUser(currentUsername);
        String username = currentUser != null ? currentUser.getUsername() : "user";

        Label welcomeLabel = new Label("Welcome " + username + "!");
        welcomeLabel.setFont(Font.font("Arial", 28));
        welcomeLabel.setStyle("-fx-font-weight: bold;");

        Label subHeading = new Label("Choose your next game adventure");
        subHeading.setFont(Font.font("Arial", 16));
        subHeading.setStyle("-fx-text-fill: #666666;");

        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(Pos.CENTER_LEFT);
        cardsContainer.setPrefWidth(Double.MAX_VALUE);
        cardsContainer.setPadding(new Insets(20, 0, 20, 0));

        // Create game cards for Tic Tac Toe, Checkers, and Connect 4.
        VBox ticTacToeCard = createGameCard("/tic_tac_toe_assets/tictactoe_images/tictactoe_icon.png", "Tic Tac Toe",
                "Quick matches of X's and O's with friends.", stage);
        VBox checkersCard = createGameCard("/checkers_assets/checkers_images/checkers_icon.png", "Checkers",
                "Strategic board game for two players.", stage);
        VBox connectFourCard = createGameCard("/connect4_assets/connect4_images/connect4_icon.png", "Connect 4",
                "Line up four in a row to claim victory.", stage);

        cardsContainer.getChildren().addAll(ticTacToeCard, checkersCard, connectFourCard);
        HBox.setHgrow(ticTacToeCard, Priority.ALWAYS);
        HBox.setHgrow(checkersCard, Priority.ALWAYS);
        HBox.setHgrow(connectFourCard, Priority.ALWAYS);

        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(15, 0, 15, 0));

        centerBox.getChildren().addAll(welcomeLabel, subHeading, cardsContainer, statsContainer);
        return centerBox;
    }

    /**
     * Creates a visual card for a game, including an icon, title, description, and play button.
     * @param iconPath Path to the game's icon resource.
     * @param gameTitle Title of the game (e.g., "Connect 4").
     * @param description Short description of the game.
     * @param stage Stage for launching the game or dialog.
     * @return A VBox representing the game card.
     */
    private VBox createGameCard(String iconPath, String gameTitle, String description, Stage stage) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        ImageView gameIcon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        StackPane iconContainer = new StackPane();
        iconContainer.getChildren().add(gameIcon);
        iconContainer.setPrefHeight(150);
        iconContainer.setStyle("-fx-background-color: black; -fx-background-radius: 10 10 0 0;");
        gameIcon.setFitHeight(80);
        gameIcon.setFitWidth(80);
        gameIcon.setPreserveRatio(true);

        VBox textContent = new VBox(8);
        textContent.setPadding(new Insets(15, 15, 15, 15));

        Label titleLabel = new Label(gameTitle);
        titleLabel.setFont(Font.font("Arial", 20));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 14));
        descLabel.setStyle("-fx-text-fill: #666666;");
        descLabel.setWrapText(true);

        // Original Play Now button
        Button playNowBtn = new Button("Play Now");
        playNowBtn.setPrefWidth(Double.MAX_VALUE);
        playNowBtn.setPadding(new Insets(10, 0, 10, 0));
        playNowBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;");

        // How to Play button
        Button howToPlayBtn = new Button("How to Play");
        howToPlayBtn.setPrefWidth(Double.MAX_VALUE);
        howToPlayBtn.setPadding(new Insets(10, 0, 10, 0));
        howToPlayBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;");

        // Add hover effects for How to Play button
        howToPlayBtn.setOnMouseEntered(e ->
                howToPlayBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 5;"));
        howToPlayBtn.setOnMouseExited(e ->
                howToPlayBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;"));

        // Add action for How to Play button
        howToPlayBtn.setOnAction(e -> showHowToPlayDialog(gameTitle, stage));

        // Existing Play Now button action
        playNowBtn.setOnAction(e -> {
            System.out.println("Play Now clicked for " + gameTitle + ", showing dialog...");
            StackPane root = (StackPane) stage.getScene().getRoot(); // Get the StackPane root for overlay
            AtomicReference<StackPane> dialogRef = new AtomicReference<>(); // To store and remove the dialog
            GameEnums.GameType gameType = getGameType(gameTitle); // Map game title to enum

            // Define actions for each match type and close button
            Runnable onLocal = () -> {
                launchGame(gameType, GameEnums.MatchType.LOCAL, stage);
                root.getChildren().remove(dialogRef.get()); // Remove dialog after action
                System.out.println("Play Local selected for " + gameTitle);
            };
            Runnable onInviteFriend = () -> {
                launchMatchmaking(gameType, GameEnums.MatchType.CASUAL_ONLINE, stage);
                root.getChildren().remove(dialogRef.get());
                System.out.println("Invite Friend selected for " + gameTitle);
            };
            Runnable onRankedMatch = () -> {
                launchMatchmaking(gameType, GameEnums.MatchType.RANKED, stage);
                root.getChildren().remove(dialogRef.get());
                System.out.println("Ranked Match selected for " + gameTitle);
            };
            Runnable onClose = () -> {
                root.getChildren().remove(dialogRef.get());
                System.out.println("Dialog closed for " + gameTitle);
            };

            // Create and display the custom dialog
            StackPane dialog = GameLobby_2.createGameModeDialog(gameTitle, onLocal, onInviteFriend, onRankedMatch, onClose);
            dialogRef.set(dialog); // Store dialog reference
            root.getChildren().add(dialog); // Overlay dialog on the menu
        });

        // Existing hover effects for Play Now button
        playNowBtn.setOnMouseEntered(e ->
                playNowBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 5;"));
        playNowBtn.setOnMouseExited(e ->
                playNowBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 5;"));

        textContent.getChildren().addAll(titleLabel, descLabel, playNowBtn, howToPlayBtn);
        card.getChildren().addAll(iconContainer, textContent);
        return card;
    }
    /**
     * Adds visual demonstrations using GIFs to the instructions.
     * @param container The VBox to add the GIF to.
     * @param gameTitle The game title to determine which GIF to load.
     */
    private void addGameDemonstrationGif(VBox container, String gameTitle) {
        try {
            String gifPath = switch (gameTitle) {
                case "Tic Tac Toe" -> "/gif/tictactoe_blue_win_line.gif";
                case "Checkers" -> "/gif/checkers_chicken_win_with_icons.gif";
                case "Connect 4" -> "/gif/connect4_blue_win_final.gif";
                default -> null;
            };

            if (gifPath != null) {
                // Create image view for the GIF
                ImageView demoGif = new ImageView(new Image(getClass().getResourceAsStream(gifPath)));
                demoGif.setFitWidth(180);
                demoGif.setPreserveRatio(true);

                // Create a container with a title for the demonstration
                VBox gifContainer = new VBox(8);
                gifContainer.setAlignment(Pos.CENTER);

                Label demoLabel = new Label("Game Demonstration");
                demoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                // Add a border and background to make the GIF stand out
                StackPane gifWrapper = new StackPane(demoGif);
                gifWrapper.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                        "-fx-border-radius: 5; -fx-padding: 5;");

                gifContainer.getChildren().addAll(demoLabel, gifWrapper);
                container.getChildren().add(gifContainer);
            }
        } catch (Exception e) {
            System.err.println("Failed to load demonstration GIF for " + gameTitle + ": " + e.getMessage());
            // Fail gracefully - if the GIF can't be loaded, the instructions will still work without it
        }
    }
    /**
     * Displays a dialog with game rules and instructions.
     * @param gameTitle The title of the game to show instructions for.
     * @param stage The parent stage.
     */
    private void showHowToPlayDialog(String gameTitle, Stage stage) {
        // Create a dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("How to Play " + gameTitle);
        dialog.initOwner(stage);

        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(500);
        content.setMaxWidth(500);

        // Title
        Label titleLabel = new Label("How to Play " + gameTitle);
        titleLabel.setFont(Font.font("Arial", 22));
        titleLabel.setStyle("-fx-font-weight: bold;");

        // Game-specific instructions
        VBox instructionsBox = new VBox(10);
        instructionsBox.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 15px; -fx-background-radius: 5;");

        // Add game-specific rules based on the game title
        switch(gameTitle) {
            case "Tic Tac Toe":
                addTicTacToeInstructions(instructionsBox);
                break;
            case "Checkers":
                addCheckersInstructions(instructionsBox);
                break;
            case "Connect 4":
                addConnect4Instructions(instructionsBox);
                break;
        }

        // Add a close button
        ButtonType closeButton = new ButtonType("Got it!", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Add all components to the content
        content.getChildren().addAll(titleLabel, instructionsBox);

        // Set the content
        dialog.getDialogPane().setContent(content);

        // Show the dialog
        dialog.showAndWait();
    }

    /**
     * Adds Tic Tac Toe instructions to the provided VBox.
     * @param container The VBox to add instructions to.
     */
    private void addTicTacToeInstructions(VBox container) {
        Label objectiveLabel = new Label("Objective:");
        objectiveLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label objective = new Label("Be the first to form a line of three of your symbols (X or O) horizontally, vertically, or diagonally on the 3×3 grid.");
        objective.setWrapText(true);

        Label rulesLabel = new Label("Rules:");
        rulesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox rulesList = new VBox(5);
        rulesList.getChildren().addAll(
                createRuleLabel("1. Players take turns placing their symbol (X or O) on an empty cell."),
                createRuleLabel("2. X usually goes first, followed by O."),
                createRuleLabel("3. Once a player places their symbol, that cell cannot be changed."),
                createRuleLabel("4. The game ends when a player forms a line of three symbols or all cells are filled (draw).")
        );
        addGameDemonstrationGif(container, "Tic Tac Toe");
        Label tipsLabel = new Label("Tips:");
        tipsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox tipsList = new VBox(5);
        tipsList.getChildren().addAll(
                createRuleLabel("• The center position is strategically valuable."),
                createRuleLabel("• Watch for your opponent's potential winning moves to block them."),
                createRuleLabel("• Try to create multiple winning opportunities simultaneously.")
        );

        container.getChildren().addAll(objectiveLabel, objective, rulesLabel, rulesList, tipsLabel, tipsList);
    }

    /**
     * Adds Checkers instructions to the provided VBox.
     * @param container The VBox to add instructions to.
     */
    private void addCheckersInstructions(VBox container) {
        Label objectiveLabel = new Label("Objective:");
        objectiveLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label objective = new Label("Capture all of your opponent's pieces or block them so they cannot make a legal move.");
        objective.setWrapText(true);

        Label rulesLabel = new Label("Rules:");
        rulesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox rulesList = new VBox(5);
        rulesList.getChildren().addAll(
                createRuleLabel("1. Pieces move diagonally forward on dark squares."),
                createRuleLabel("2. Capture opponent's pieces by jumping over them diagonally."),
                createRuleLabel("3. Multiple jumps in a single turn are allowed if possible."),
                createRuleLabel("4. When a piece reaches the opponent's back row, it becomes a King."),
                createRuleLabel("5. Kings can move and capture diagonally in any direction.")
        );
        addGameDemonstrationGif(container, "Checkers");
        Label tipsLabel = new Label("Tips:");
        tipsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox tipsList = new VBox(5);
        tipsList.getChildren().addAll(
                createRuleLabel("• Control the center of the board."),
                createRuleLabel("• Keep your pieces grouped for protection."),
                createRuleLabel("• Try to get Kings as early as possible."),
                createRuleLabel("• Force your opponent to make moves that benefit you.")
        );

        container.getChildren().addAll(objectiveLabel, objective, rulesLabel, rulesList, tipsLabel, tipsList);
    }

    /**
     * Adds Connect 4 instructions to the provided VBox.
     * @param container The VBox to add instructions to.
     */
    private void addConnect4Instructions(VBox container) {
        Label objectiveLabel = new Label("Objective:");
        objectiveLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label objective = new Label("Be the first to form a horizontal, vertical, or diagonal line of four of your colored discs.");
        objective.setWrapText(true);

        Label rulesLabel = new Label("Rules:");
        rulesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox rulesList = new VBox(5);
        rulesList.getChildren().addAll(
                createRuleLabel("1. Players take turns dropping one colored disc from the top into a seven-column, six-row grid."),
                createRuleLabel("2. The pieces fall straight down, occupying the lowest available space within the column."),
                createRuleLabel("3. The game ends when a player forms a line of four discs or the grid is full (draw).")
        );
        addGameDemonstrationGif(container, "Connect 4");
        Label tipsLabel = new Label("Tips:");
        tipsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox tipsList = new VBox(5);
        tipsList.getChildren().addAll(
                createRuleLabel("• The center column is strategically valuable."),
                createRuleLabel("• Watch for your opponent's potential winning moves to block them."),
                createRuleLabel("• Try to create threats in multiple directions simultaneously."),
                createRuleLabel("• Plan at least 2-3 moves ahead.")
        );

        container.getChildren().addAll(objectiveLabel, objective, rulesLabel, rulesList, tipsLabel, tipsList);
    }

    /**
     * Creates a formatted label for a rule or tip.
     * @param text The rule or tip text.
     * @return A formatted Label.
     */
    private Label createRuleLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #333333;");
        return label;
    }


    /**
     * Maps a game title to its corresponding GameType enum.
     * @param gameTitle The title of the game (e.g., "Connect 4").
     * @return The matching GameEnums.GameType value.
     * @throws IllegalArgumentException if the title is unrecognized.
     */
    private GameEnums.GameType getGameType(String gameTitle) {
        switch (gameTitle) {
            case "Connect 4":
                return GameEnums.GameType.CONNECT_FOUR;
            case "Tic Tac Toe":
                return GameEnums.GameType.TIC_TAC_TOE;
            case "Checkers":
                return GameEnums.GameType.CHECKERS;
            default:
                System.err.println("Unknown game title: " + gameTitle);
                throw new IllegalArgumentException("Unknown game title: " + gameTitle);
        }
    }

    /**
     * Launches a game directly in Local mode with default player names.
     * @param gameType The type of game to launch.
     * @param matchType The match type (e.g., LOCAL).
     * @param stage The current stage to close after launching the game.
     */
    private void launchGame(GameEnums.GameType gameType, GameEnums.MatchType matchType, Stage stage) {
        System.out.println("Launching game: " + gameType + " as " + matchType);
        launchGameScreen(gameType, matchType, "Player 1", "Player 2", stage);
    }

    /**
     * Simulates matchmaking by launching a game with a placeholder opponent name.
     * @param gameType The type of game to launch.
     * @param matchType The match type (CASUAL_ONLINE or RANKED).
     * @param stage The current stage to close after launching.
     */
    private void launchMatchmaking(GameEnums.GameType gameType, GameEnums.MatchType matchType, Stage stage) {
        String opponent = matchType == GameEnums.MatchType.CASUAL_ONLINE ? "Friend" : "Opponent";
        System.out.println("Launching matchmaking: " + gameType + " as " + matchType);
        launchGameScreen(gameType, matchType, "Player", opponent, stage);
    }

    /**
     * Launches the game screen for the selected game and match type.
     * @param gameType The type of game (e.g., CONNECT_FOUR).
     * @param matchType The match type (e.g., LOCAL, CASUAL_ONLINE, RANKED).
     * @param player1 Name of the first player.
     * @param player2 Name of the second player or opponent.
     * @param stage The current stage to close after launching the new game stage.
     */
    private void launchGameScreen(GameEnums.GameType gameType, GameEnums.MatchType matchType, String player1, String player2, Stage stage) {
        try {
            String fxmlPath;
            switch (gameType) {
                case CONNECT_FOUR:
                    fxmlPath = "/connect4_assets/Connect4Screen.fxml";
                    break;
                case TIC_TAC_TOE:
                    fxmlPath = "/tic_tac_toe_assets/TicTacToeScreen.fxml";
                    break;
                case CHECKERS:
                    fxmlPath = "/checkers_assets/CheckersScreen.fxml";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown game type: " + gameType);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            System.out.println("FXML path: " + fxmlPath + ", URL: " + (loader.getLocation() != null ? loader.getLocation().toString() : "null"));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            Scene scene = new Scene(loader.load(), 1440, 1024);
            Stage gameStage = new Stage();
            gameStage.setScene(scene);
            gameStage.setTitle(gameType.toString().replace('_', ' '));
            gameStage.setResizable(false);

            Object controller = loader.getController();
            if (controller instanceof ConnectFourController) {
                ConnectFourController cfc = (ConnectFourController) controller;
                cfc.setMatchType(matchType);
                cfc.setPlayerNames(player1, player2);
            } else if (controller instanceof TicTacToeController) { // Added for TicTacToe integration
                TicTacToeController ttc = (TicTacToeController) controller;
                ttc.setMatchType(matchType);
                ttc.setPlayerNames(player1, player2);
            } else if (controller instanceof CheckersController) {
                CheckersController cc = (CheckersController) controller;
                cc.setMatchType(matchType);
                cc.setPlayerNames(player1, player2);
                cc.startGame();
            }

            gameStage.show();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Launch Game");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}