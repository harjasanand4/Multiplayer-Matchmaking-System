package com.game.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.game.gui.UIUtils.launchFeature;

public class GameMenu {

    private Scene loginScene; // Store the LoginWindow scene for back navigation

    public Scene createScene(Stage stage) {
        // Use StackPane to layer background image and form
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);

        // Background image
        Image backgroundImage = new Image("/MenuBackground.gif");
        // Create an ImageView for the background
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false); // Allow stretching
        backgroundImageView.setSmooth(false);
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());

        // Title
        Label title = new Label("Board Game Hub");
        title.setFont(Font.font("Arial", 42));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(5, Color.BLACK));

        // Game Buttons
        Button checkersButton = createStyledButton("Checkers", "#8B4513");
        Button ticTacToeButton = createStyledButton("Tic Tac Toe", "#4682B4");
        Button connect4Button = createStyledButton("Connect 4", "#DC143C");

        // Button actions (launching games in new windows for now)
        checkersButton.setOnAction(e -> launchLobby("Checkers", stage));
        ticTacToeButton.setOnAction(e -> launchLobby("Tic Tac Toe", stage));
        connect4Button.setOnAction(e -> launchLobby("Connect 4", stage));

        //Feature Buttons
        Button settingsButton = createImageButton("/menu_buttons/settings.png", "Settings", stage);
        Button userProfileButton = createImageButton("/menu_buttons/profile.png", "User Profile", stage);
        Button leaderboardButton = createImageButton("/menu_buttons/leaderboard_button_icon_large.png", "Leaderboard", stage);
        Button friendsButton = createImageButton("/menu_buttons/friends.png", "Friends", stage);
        Button chatsButton = createImageButton("/menu_buttons/chat.png", "Chats", stage);

        //A Search a friend
        TextField searchField = new TextField();
        searchField.setPromptText("Search a Friend");
        searchField.setPrefWidth(180);

        // Exit Button
        Button exitButton = createStyledButton("Exit", "#696969");
        exitButton.setOnAction(e -> System.exit(0));

        // Back to Login Button
        Button backButton = createStyledButton("Back to Login", "#808080");
        backButton.setOnAction(e -> {
            if (loginScene != null) {
                stage.setScene(loginScene);
                Main.StageManager.configureStage(stage);
                stage.setTitle("Login");
            }
        });

        //Top Bar for userProfile, chats, leaderboard, search, settings
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add elements to formPane
        topBar.getChildren().addAll(
                userProfileButton,
                chatsButton,
                leaderboardButton,
                friendsButton,
                spacer,
                searchField,
                settingsButton
        );
        //Centre Box that holds title and HBox of Game Buttons
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.CENTER);
        HBox gameButtonsRow = new HBox(60);
        gameButtonsRow.setAlignment(Pos.CENTER);
        gameButtonsRow.getChildren().addAll(checkersButton, connect4Button, ticTacToeButton);


        centerBox.getChildren().addAll(title, gameButtonsRow);

        //Build a bottom bar for “Back to Login” (left) and “Exit” (right) --
        // One simple approach: use a BorderPane in the bottom region
        BorderPane bottomPane = new BorderPane();
        bottomPane.setPadding(new Insets(10));

        //Left side: backButton
        HBox leftBottom = new HBox(backButton);
        leftBottom.setAlignment(Pos.CENTER_LEFT);

        //Right side: exitButton
        HBox rightBottom = new HBox(exitButton);
        rightBottom.setAlignment(Pos.CENTER_RIGHT);

        bottomPane.setLeft(leftBottom);
        bottomPane.setRight(rightBottom);

        //Put it all together in a BorderPane (on top of the StackPane)
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setCenter(centerBox);
        mainLayout.setBottom(bottomPane);

        //Add background image and main layout to the root StackPane
        root.getChildren().addAll(backgroundImageView, mainLayout);
        Main.StageManager.configureStage(stage);
        stage.setResizable(true);  // Allow resizing

        //Remember the original login scene for “Back to Login”
        loginScene = stage.getScene();

        //Return the new scene --
        return new Scene(root, 950, 620);
    }

    private Button createStyledButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", 20));
        button.setPrefSize(250, 60);
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

    private void launchLobby(String gameName, Stage stage) {
        try {
            // Create an instance of the common GameLobby.
            // You can pass the username or other shared info to its constructor if needed.
            GameLobby lobby = new GameLobby("User_name", stage);

            // Dynamically update the lobby title to match the selected game.
            lobby.setGameName(gameName);


            // Create a new stage for the lobby and set the scene.
            Stage lobbyStage = new Stage();
            Scene scene = new Scene(lobby, 950, 620);

            // Using StageManager to prepare and show the stage
            Main.StageManager.showStage(lobbyStage, scene);

            Platform.runLater(() -> {
                // Close the original stage after a slight delay
                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(e -> stage.close());
                delay.play();
            });
        } catch (Exception e) {
            System.out.println("Error launching " + gameName + ": " + e.getMessage());
        }
    }


    private Button createImageButton(String imagePath, String featureName, Stage stage) {
        // Load the image
        Image image = new Image(imagePath);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(60); // Adjust size as needed
        imageView.setFitHeight(60); // Adjust size as needed
        imageView.setPreserveRatio(true);

        // Create button with image
        Button button = new Button();
        button.setGraphic(imageView);
        button.setPrefSize(80, 80); // Slightly larger than image for padding
        button.setStyle(
                "-fx-background-color: transparent;" + // No background
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;"
        );
        button.setEffect(new DropShadow(3, Color.GRAY));

        // Hover effect (slight scale increase)
        button.setOnMouseEntered(e -> {
            imageView.setScaleX(1.1);
            imageView.setScaleY(1.1);
        });
        button.setOnMouseExited(e -> {
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        });

        // Action to launch feature
        button.setOnAction(e -> launchFeature(featureName, stage));

        return button;
    }

}