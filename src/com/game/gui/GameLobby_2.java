package com.game.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GameLobby_2 {

    /**
     * Makes a small popup with game mode options and a close button.
     *
     * @param gameTitle the name of the game to show in the title
     * @param onLocal what to do when "Play Local" is clicked
     * @param onInviteFriend what to do when "Invite Friend" is clicked
     * @param onRankedMatch what to do when "Ranked Match" is clicked
     * @param onClose what to do when the close (X) is clicked
     * @return a StackPane showing the popup
     */
    public static StackPane createGameModeDialog(String gameTitle, Runnable onLocal, Runnable onInviteFriend, Runnable onRankedMatch, Runnable onClose) {
        // Dark background behind the popup
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // White box for the popup
        VBox dialogBox = new VBox(15);
        dialogBox.setPadding(new Insets(20, 30, 20, 30));
        dialogBox.setPrefWidth(600);
        dialogBox.setMaxWidth(600);
        dialogBox.setMaxHeight(180);
        dialogBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 4);");

        // Red close button (X)
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold;");
        closeBtn.setPadding(new Insets(4, 8, 4, 8));
        closeBtn.setOnAction(e -> { onClose.run(); });

        // Title text for the popup
        Label titleLabel = new Label("How do you want to Play " + gameTitle + "?");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_CENTER);

        // Header with title and close button
        StackPane header = new StackPane();
        header.setPrefWidth(Double.MAX_VALUE);
        header.getChildren().addAll(titleLabel, closeBtn);
        StackPane.setAlignment(titleLabel, Pos.CENTER);
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);

        // Line between title and buttons
        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 0, 5, 0));

        // Row for the three buttons
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);

        Button btnLocal = new Button("Play Local");
        Button btnInviteFriend = new Button("Invite Friend");
        Button btnRanked = new Button("Ranked Match");

        // Style each game mode button and add hover effects
        for (Button btn : new Button[]{btnLocal, btnInviteFriend, btnRanked}) {
            btn.setFont(Font.font("Arial", 18));
            btn.setWrapText(true);
            btn.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-background-radius: 5;");
            btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-background-radius: 5;")
            );
            btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-background-radius: 5;")
            );
        }

        btnRanked.setOnAction(event -> {
            MatchmakingWindow2 matchmaking = new MatchmakingWindow2(gameTitle, MatchmakingWindow2.MatchType.RANKED);
            matchmaking.start(new Stage());
        });

        btnInviteFriend.setOnAction(event -> {
            MatchmakingWindow2 matchmaking = new MatchmakingWindow2(gameTitle, MatchmakingWindow2.MatchType.FRIEND);
            matchmaking.start(new Stage());
        });

        btnLocal.setOnAction(event -> {
            MatchmakingWindow2 matchmaking = new MatchmakingWindow2(gameTitle, MatchmakingWindow2.MatchType.LOCAL);
            matchmaking.start(new Stage());
        });

        buttonRow.getChildren().addAll(btnLocal, btnInviteFriend, btnRanked);

        // Add everything into the white popup
        dialogBox.getChildren().addAll(header, separator, buttonRow);
        overlay.getChildren().add(dialogBox);

        // Add popup to the background and center it
        StackPane.setAlignment(dialogBox, Pos.CENTER);

        return overlay;
    }
}