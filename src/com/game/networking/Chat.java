package com.game.networking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class regarding the chat in-game feature
 */
public class Chat {
    private String messageContent;
    private Player sender;
    private Player recipient;
    private LocalDateTime timeStamp;
    private String formattedTimeStamp;
    private static final int MAX_MESSAGE_LENGTH = 200;

    /**
     * Creates a chat object containing the timestamp, message, and the player who sent it
     * @param messageContent
     * @param sender
     * @param recipient
     */
    public Chat(String messageContent, Player sender, Player recipient) {
        if (messageContent == null || messageContent.trim().isEmpty()) {
            System.err.println("Unable to send an empty message.");
            this.messageContent = ""; // Default value
        }
        else if (messageContent.length() > MAX_MESSAGE_LENGTH) {
            System.err.println("Message exceeds maximum length of " + MAX_MESSAGE_LENGTH + " characters.");
            this.messageContent = messageContent.substring(0, MAX_MESSAGE_LENGTH); // Truncate
        }
        else {
            // Only set if validation passes
            this.messageContent = messageContent;
            this.sender = sender;
            this.recipient = recipient;
            updateTimestamp();
        }
    }

    /**
     * Getter for sender
     * @return Player object of sender
     */
    public Player getSender() {
        return sender;
    }

    /**
     * Getter for recipient
     * @return Player object of recipient
     */
    public Player getRecipient() {
        return recipient;
    }

    /**
     * Getter for message
     * @return String of message contents
     */
    public String getMessageContent() {
        return messageContent;
    }

    /**
     * Getter for message time stamp
     * @return LocalDateTime object of time
     */
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * Getter for message time stamp in String format
     * @return String of formatted time stamp
     */
    public String getFormattedTimeStamp() {
        return formattedTimeStamp;
    }

    /**
     * Helper method to update both timeStamp and formattedTimeStamp
     */
    private void updateTimestamp() {
        this.timeStamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.formattedTimeStamp = timeStamp.format(formatter);
    }

    /**
     * Formats chat message by including timeStamp, player username, and message content
     * @return String of sent message
     */
    @Override
    public String toString() {
        return formattedTimeStamp + " - " + sender.getUsername() + ": " + getMessageContent();
    }
}
