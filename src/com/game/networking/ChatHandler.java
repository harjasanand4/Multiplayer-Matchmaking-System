package com.game.networking;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class that manages the last 5 chats using a FIFO queue
 */
public class ChatHandler {
    private final Queue<Chat> chatQueue = new LinkedList<>();
    private static final int MAX_CHAT_HISTORY_SIZE = 5; // Limit to 5 recent chats

    /**
     * Adds a chat to history, removing the oldest if full
     */
    public synchronized void addChat(Chat chat) {
        if (chatQueue.size() >= MAX_CHAT_HISTORY_SIZE) {
            chatQueue.poll();
        }
        chatQueue.offer(chat); // Add new chat
    }

    /**
     * Returns all chats as separate lines
     * @return String of chat history
     */
    public synchronized String getFormattedHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chat History:\n");

        // Iterate in natural queue order (oldest to newest)
        for (Chat chat : chatQueue) {
            sb.append(chat).append("\n");
        }
        return sb.toString();
    }
}