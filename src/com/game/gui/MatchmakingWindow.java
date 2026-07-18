package com.game.gui;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MatchmakingWindow extends BorderPane {

    public enum MatchType {
        RANDOM,
        RANKED,
        FRIEND,
        LOCAL
    }

    private String gameName;
    private MatchType matchType;
    private Label message;
    private Label eloLabel;
    private ProgressIndicator spinner;
    private VBox layout;
    private boolean isCancelled = false;

    public MatchmakingWindow(String gameName, MatchType matchType) {
        this.gameName = gameName;
        this.matchType = matchType;
    }

    public void start(Stage stage) {
        message = new Label(getMatchmakingMessage());
        message.setStyle("-fx-font-size: 24px;");

        spinner = new ProgressIndicator();
        spinner.setPrefSize(80, 80);

        eloLabel = new Label();
        eloLabel.setStyle("-fx-font-size: 16px;");

        layout = new VBox(20, message, spinner);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 300);

        Button cancelButton = new Button("Cancel Matchmaking");
        cancelButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 8px;");
        cancelButton.setOnAction(e -> {
            isCancelled = true;
            GameLobby gameLobby = new GameLobby("YourUserName", stage); // Provide the user name
            gameLobby.setGameName(gameName);
            Scene lobbyScene = new Scene(gameLobby, 950, 620); // or screenBounds.getWidth(), getHeight()
            stage.setScene(lobbyScene);
            stage.setTitle(gameName + " Lobby");
            stage.show();
        });
        layout.getChildren().add(cancelButton);

        Scene scene = new Scene(layout);
        stage.setTitle("Matchmaking");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

        switch (matchType) {
            case RANKED -> handleRankedMatch(stage);
            case FRIEND -> handleFriendMatch(stage);
            case LOCAL -> launchGame(stage); // skip matchmaking
            case RANDOM -> simulateQuickMatch(stage);
        }
    }

    private void handleRankedMatch(Stage stage) {
        layout.getChildren().add(eloLabel);
        eloLabel.setText("Searching within ±200 ELO...");

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> eloLabel.setText("Searching within ±400 ELO...")),
            new KeyFrame(Duration.seconds(60), e -> showTimeoutOptions(stage))
        );
        timeline.setCycleCount(1);
        timeline.play();

        simulateMatchFound(stage, Duration.seconds(10));
    }

    private void handleFriendMatch(Stage stage) {
        simulateMatchFound(stage, Duration.seconds(5), "Friend123");
    }

    private void simulateQuickMatch(Stage stage) {
        simulateMatchFound(stage, Duration.seconds(3));
    }

    private void simulateMatchFound(Stage stage, Duration delay) {
        simulateMatchFound(stage, delay, "Opponent_42");
    }

    private void simulateMatchFound(Stage stage, Duration delay, String opponentName) {
        PauseTransition wait = new PauseTransition(delay);
        wait.setOnFinished(e -> {
            if (isCancelled) return;

            message.setText("Match found! Opponent: @" + opponentName);
            spinner.setProgress(1.0);

            PauseTransition launchDelay = new PauseTransition(Duration.seconds(2));
            launchDelay.setOnFinished(ev -> {
                if (!isCancelled) {
                    launchGame(stage);
                }
            });
            launchDelay.play();
        });
        wait.play();
    }


    private void showTimeoutOptions(Stage stage) {
        message.setText("No opponent found.");
        spinner.setProgress(0);

        Button retryBtn = new Button("Retry");
        retryBtn.setOnAction(e -> {
            stage.close();
            new MatchmakingWindow(gameName, matchType).start(new Stage());
        });

        Button dashboardBtn = new Button("Return to Dashboard");
        dashboardBtn.setOnAction(e -> stage.close());

        Button randomBtn = new Button("Enter Random Match");
        randomBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                "You may be paired with a player significantly above or below your rank. This may affect your ELO score.",
                ButtonType.OK);
            alert.setTitle("Random Match Warning");
            alert.showAndWait();
            stage.close();
            new MatchmakingWindow(gameName, MatchType.RANDOM).start(new Stage());
        });

        layout.getChildren().addAll(retryBtn, dashboardBtn, randomBtn);
    }

    private void launchGame(Stage stage) {
        stage.close();
        try {
            switch (gameName.trim().toLowerCase()) {
                case "tic tac toe" -> new com.game.gui.TicTacToe(new Stage()).showLobby();
                case "connect 4" -> new Connect4(new Stage()).showLobby();
                case "checkers" -> new Checkers().start(new Stage());
                default -> System.out.println("Unknown game: " + gameName);
            }
        } catch (Exception ex) {
            System.out.println("Error starting game: " + ex.getMessage());
        }
    }

    private String getMatchmakingMessage() {
        return switch (matchType) {
            case RANKED -> "Finding a player of similar rank...";
            case FRIEND -> "Waiting for a friend to accept...";
            case RANDOM -> "Searching for a player...";
            case LOCAL -> "Launching local game...";
        };
    }
}


