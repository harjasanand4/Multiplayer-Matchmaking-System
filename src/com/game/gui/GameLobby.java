package com.game.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

import static com.game.gui.UIUtils.launchFeature;

/**
 * A JavaFX game lobby pane designed to match the provided wireframe.
 */
public class GameLobby extends BorderPane {

    private Label titleLabel;
    private ImageView avatarImageView;
    private Label userNameLabel;
    private Button playRandomButton;
    private Button playByRankButton;
    private Button challengeFriendButton;
    private String gameName;
    private Stage stage;
    private String userName;
    private Stage parentStage; // Reference to the parent stage for transparency effect

    /**
     * Constructor for the GameLobby.
     *
     * @param userName The username to display in the lobby.
     * @param parentStage The parent stage to make translucent when the lobby pops up.
     */
    public GameLobby(String userName, Stage parentStage) {
        this.userName = userName;
        this.parentStage = parentStage;
        this.stage = new Stage();
        initUI();
    }

    /**
     * Initializes the UI components and layout.
     */
    private void initUI() {
        // Background image
        Image backgroundImage = new Image(GameMenu.class.getResource("/MenuBackground.gif").toExternalForm());
        BackgroundImage bImg = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true));
        this.setBackground(new Background(bImg));

        // Top bar with user profile, leaderboard, and stats
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        // Avatar and username
        try {
            Image avatarImage = new Image(GameMenu.class.getResource("/profile_pictures/avatar_0.png").toExternalForm());
            avatarImageView = new ImageView(avatarImage);
        } catch (Exception e) {
            avatarImageView = new ImageView();
            StackPane placeholder = new StackPane();
            placeholder.setStyle("-fx-background-color: #4682B4; -fx-background-radius: 50;");
            placeholder.setPrefSize(40, 40);
            Label initials = new Label(userName.substring(0, 1).toUpperCase());
            initials.setTextFill(Color.WHITE);
            initials.setFont(Font.font("Arial", 18));
            placeholder.getChildren().add(initials);
            avatarImageView = new ImageView();
        }
        avatarImageView.setFitWidth(40);
        avatarImageView.setFitHeight(40);
        avatarImageView.setPreserveRatio(true);

        userNameLabel = new Label(userName + " Online");
        userNameLabel.setFont(Font.font("Arial", 14));
        userNameLabel.setTextFill(Color.WHITE);
        userNameLabel.setEffect(new DropShadow(3, Color.BLACK));

        HBox userProfileBox = new HBox(5, avatarImageView, userNameLabel);
        userProfileBox.setAlignment(Pos.CENTER_LEFT);

        // Top bar buttons
        Button leaderboardButton = createImageButton("/menu_buttons/leaderboard_button_icon_large.png", "Leaderboard", stage);
        Button myStatsButton = createImageButton("/menu_buttons/stats.png", "My Stats", stage); // Assuming a stats icon
        Button notificationsButton = createImageButton("/menu_buttons/notification.png", "Notifications", stage); // Assuming a notification icon

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBox.getChildren().addAll(userProfileBox, leaderboardButton, myStatsButton, notificationsButton, spacer);

        // Center grid for game icons
        GridPane gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(20);
        gameGrid.setVgap(20);
        gameGrid.setPadding(new Insets(20));

        // Game icons
        Image ticTacToeIcon = new Image(GameMenu.class.getResource("/tic_tac_toe_assets/tic_tac_toe.png").toExternalForm());
        ImageView ticTacToeView = new ImageView(ticTacToeIcon);
        ticTacToeView.setFitWidth(100);
        ticTacToeView.setFitHeight(100);
        ticTacToeView.setOnMouseClicked(e -> showGameModePopup("Tic Tac Toe"));
        ticTacToeView.setStyle("-fx-cursor: hand;");

        Image checkersIcon = new Image(GameMenu.class.getResource("/checkers_assets/checkers.png").toExternalForm());
        ImageView checkersView = new ImageView(checkersIcon);
        checkersView.setFitWidth(100);
        checkersView.setFitHeight(100);
        checkersView.setOnMouseClicked(e -> showGameModePopup("Checkers"));
        checkersView.setStyle("-fx-cursor: hand;");


        gameGrid.add(ticTacToeView, 0, 0);
        gameGrid.add(checkersView, 1, 0);

        // Bottom section with recent matches and stats
        VBox bottomLeft = new VBox(10);
        bottomLeft.setAlignment(Pos.CENTER_LEFT);
        Label recentMatchesLabel = new Label("Recent Matches");
        recentMatchesLabel.setTextFill(Color.WHITE);
        Label recentMatch1 = new Label("Tic Tac Toe vs Computer");
        recentMatch1.setTextFill(Color.WHITE);
        Label recentMatch2 = new Label("Victory");
        recentMatch2.setTextFill(Color.WHITE);
        bottomLeft.getChildren().addAll(recentMatchesLabel, recentMatch1, recentMatch2);

        VBox bottomRight = new VBox(10);
        bottomRight.setAlignment(Pos.CENTER_RIGHT);
        Label statsLabel = new Label("Stats");
        statsLabel.setTextFill(Color.WHITE);
        Label totalWins = new Label("Total Wins: 127");
        totalWins.setTextFill(Color.WHITE);
        Label ranking = new Label("Ranking: #234");
        ranking.setTextFill(Color.WHITE);
        Label friendsOnline = new Label("Friends Online: 12");
        friendsOnline.setTextFill(Color.WHITE);
        bottomRight.getChildren().addAll(statsLabel, totalWins, ranking, friendsOnline);

        HBox bottomBox = new HBox(20, bottomLeft, bottomRight);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));

        // Set up the BorderPane structure
        this.setTop(topBox);
        this.setCenter(gameGrid);
        this.setBottom(bottomBox);

        // Create the scene
        Scene lobbyScene = new Scene(this, 950, 620);
        stage.setTitle("OMG Platform Lobby");
        stage.setScene(lobbyScene);
        Main.StageManager.configureStage(stage);

        // Show the lobby and make the parent stage translucent
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(parentStage);
        stage.initStyle(StageStyle.TRANSPARENT); // Allows custom transparency
        makeParentTranslucent(parentStage);
        stage.show();

        stage.setOnCloseRequest(e -> restoreParentOpacity(parentStage));
    }

    /**
     * Makes the parent stage translucent when the lobby is shown.
     */
    private void makeParentTranslucent(Stage parentStage) {
        if (parentStage != null) {
            parentStage.getScene().getRoot().setEffect(new DropShadow(20, Color.BLACK));
            parentStage.getScene().setFill(Color.rgb(0, 0, 0, 0.5)); // Semi-transparent black overlay
        }
    }

    /**
     * Restores the parent stage's opacity when the lobby is closed.
     */
    private void restoreParentOpacity(Stage parentStage) {
        if (parentStage != null) {
            parentStage.getScene().getRoot().setEffect(null);
            parentStage.getScene().setFill(Color.BLACK); // Restore to original
        }
    }

    /**
     * Shows a popup for selecting game mode.
     */
    private void showGameModePopup(String gameName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(gameName);
        dialog.setHeaderText("How do you want to Play " + gameName + "?");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        VBox dialogContent = new VBox(10);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(10));

        Button playVsComputer = createStyledButton("Play vs Computer", "#4682B4");
        playVsComputer.setOnAction(e -> {
            Stage matchmakingStage = new Stage();
            MatchmakingWindow mm = new MatchmakingWindow(gameName, MatchmakingWindow.MatchType.RANDOM);
            mm.start(matchmakingStage);
            dialog.close();
        });

        Button inviteFriend = createStyledButton("Invite Friend", "#8B4513");
        inviteFriend.setOnAction(e -> {
            Stage matchmakingStage = new Stage();
            MatchmakingWindow mm = new MatchmakingWindow(gameName, MatchmakingWindow.MatchType.FRIEND);
            mm.start(matchmakingStage);
            dialog.close();
        });

        Button rankedMatch = createStyledButton("Ranked Match", "#DC143C");
        rankedMatch.setOnAction(e -> {
            Stage matchmakingStage = new Stage();
            MatchmakingWindow mm = new MatchmakingWindow(gameName, MatchmakingWindow.MatchType.RANKED);
            mm.start(matchmakingStage);
            dialog.close();
        });

        dialogContent.getChildren().addAll(playVsComputer, inviteFriend, rankedMatch);
        dialog.getDialogPane().setContent(dialogContent);
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.showAndWait();
    }

    /**
     * Creates a styled button with hover effects.
     */
    private Button createStyledButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", 20));
        button.setPrefSize(200, 50);
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + baseColor + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;"
        );
        button.setEffect(new DropShadow(3, Color.GRAY));

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + baseColor + ", 20%);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + baseColor + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;"
        ));

        return button;
    }

    /**
     * Creates an image button with hover effects.
     */
    private Button createImageButton(String imagePath, String featureName, Stage stage) {
        Image image = new Image(GameMenu.class.getResource(imagePath).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);

        Button button = new Button();
        button.setGraphic(imageView);
        button.setPrefSize(50, 50);
        button.setStyle("-fx-background-color: transparent;");
        button.setEffect(new DropShadow(3, Color.GRAY));

        button.setOnMouseEntered(e -> {
            imageView.setScaleX(1.1);
            imageView.setScaleY(1.1);
        });
        button.setOnMouseExited(e -> {
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        });

        button.setOnAction(e -> launchFeature(featureName, stage));

        return button;
    }

    /**
     * Updates the title label of the lobby to display the selected game name.
     */
    public void setGameName(String newGameName) {
        this.gameName = newGameName;
        titleLabel.setText(newGameName + " Lobby");
    }

    /**
     * Shows the lobby stage.
     */
    public void show() {
        stage.show();
    }
}