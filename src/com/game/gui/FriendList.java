package com.game.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.swing.text.Element;

public class FriendList extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Friend List");

        BorderPane layout = new BorderPane();

        // Top bar and footer provided by UIUtils helper
        layout.setTop(UIUtils.createTopBar(primaryStage));
        layout.setCenter(createFriendListContent(primaryStage));
        layout.setBottom(UIUtils.createFooter());

        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the main friend list UI shown in the center of the screen.
     */
    private VBox createFriendListContent(Stage stage) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #F5F5F5;");

        // --- Title + Search bar + Add Friend button ---
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Friends");
        titleLabel.setFont(Font.font("Arial", 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search friends..."); // TODO: Connect to search functionality

        Button addFriendButton = new Button("Add Friend");
        addFriendButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        // TODO: Connect add friend action

        headerRow.getChildren().addAll(titleLabel, spacer, searchField, addFriendButton);

        // --- Online Friends Section ---
        VBox onlineSection = new VBox(10);
        Label onlineLabel = new Label("Online (3)");
        onlineLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444;");
        onlineSection.getChildren().add(onlineLabel);

        // TODO: Replace with dynamic data from backend or state manager
        onlineSection.getChildren().addAll(
                createFriendCard("Sarah123", "Playing Connect 4", true, true),
                createFriendCard("Mike454", "Available", true, true),
                createFriendCard("Player898", "Available", true, true)
        );

        // --- Offline Friends Section ---
        VBox offlineSection = new VBox(10);
        Label offlineLabel = new Label("Offline (2)");
        offlineLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444;");
        offlineSection.getChildren().add(offlineLabel);

        // TODO: Replace with real friend data + online status tracking
        offlineSection.getChildren().addAll(
                createFriendCard("Emma444", "Last online 2h ago", false, false),
                createFriendCard("Tommy78", "Last online 5h ago", false, false)
        );

        // Wrap all friend groups in one white card-like container
        VBox cardContainer = new VBox(20, onlineSection, offlineSection);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);"
        );

        container.getChildren().addAll(headerRow, cardContainer);
        return container;
    }

    /**
     * Creates a card representing a single friend with their status and invite button.
     */
    private HBox createFriendCard(String name, String status, boolean isOnline, boolean showInvite) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 8;");

//        ImageView avatar = new ImageView("/profile_pictures/avatar_0.png");
//        avatar.setFitHeight(40);
//        avatar.setFitWidth(40);
//        avatar.setStyle("-fx-background-radius: 50%;");

        VBox infoBox = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 14px;" + (isOnline ? "" : " -fx-text-fill: gray;"));

        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-size: 12px;" +
                (isOnline ? " -fx-text-fill: green;" : " -fx-text-fill: #999;"));

        infoBox.getChildren().addAll(nameLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button inviteBtn = new Button("Invite to Play");
        inviteBtn.setVisible(showInvite);
        inviteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");
        inviteBtn.setOnAction(e -> {
            // TODO: Replace with actual invitation logic
            System.out.println("Invite sent to " + name);
        });

        card.getChildren().addAll( infoBox, spacer);
        if (showInvite) card.getChildren().add(inviteBtn);

        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
