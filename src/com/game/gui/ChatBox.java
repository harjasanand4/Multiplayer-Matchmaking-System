package com.game.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatBox extends Application {

    private final ListView<String> messageList = new ListView<>();
    private final TextField messageField = new TextField();
    private final ObservableList<String> messages = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // Initialize and configure the message list
        messageList.setItems(messages);
        messageList.setPrefHeight(500);
        messageList.setFocusTraversable(false);

        // Configure the message input field
        messageField.setPromptText("Type a message...");
        messageField.setPrefWidth(500);

        // Send button
        Button sendButton = new Button("Send");
        sendButton.setOnAction(event -> sendMessage());

        // Allow pressing "Enter" to send a message
        messageField.setOnAction(event -> sendMessage());

        // Layout setup
        HBox inputArea = new HBox(10, messageField, sendButton);
        inputArea.setPrefHeight(40);
        inputArea.setStyle("-fx-padding: 10; -fx-background-color: #444444;");

        VBox chatLayout = new VBox(10, messageList, inputArea);
        chatLayout.setStyle("-fx-padding: 10; -fx-background-color: #333333;");

        // Root pane setup
        Pane root = new Pane(chatLayout);
        chatLayout.setLayoutX(50);
        chatLayout.setLayoutY(20);

        // Scene setup
        Scene scene = new Scene(root, 700, 600);
        stage.setTitle("ChatBox");
        stage.setScene(scene);
        stage.show();

        // Auto-scroll to latest message
        messages.addListener((javafx.collections.ListChangeListener<? super String>) c -> {
            messageList.scrollTo(messages.size() - 1);
        });
    }

    /**
     * Appends a new message to the chat display.
     * Note: The messages are stored only during the current session.
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            messages.add("You: " + message);  // Replace "You" with the player's name if needed
            messageField.clear();
        }
    }

    /**
     * Displays a temporary notification pop-up with the given message.
     * This simulates a notification mark or pop-up.
     */
    private void showNotification(String message) {
        messages.add("System: " + message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
