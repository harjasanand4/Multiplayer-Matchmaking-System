package com.game.networking;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the entire com.game.networking.chathandler class
 */
public class ChatHandlerTest {
    private ChatHandler handler;
    private Chat chat1, chat2, chat3, chat4, chat5, chat6;
    private Player dummyPlayer;

    /**
     * Sets up a player object to create multiple chat to put into chatHandler.
     */
    @BeforeEach
     public void setUp() {
        handler = new ChatHandler();
        dummyPlayer = new Player("0", "Test", "test@test.com", "pwd", null);
        chat1 = new Chat("Msg1", dummyPlayer, dummyPlayer);
        chat2 = new Chat("Msg2", dummyPlayer, dummyPlayer);
        chat3 = new Chat("Msg3", dummyPlayer, dummyPlayer);
        chat4 = new Chat("Msg4", dummyPlayer, dummyPlayer);
        chat5 = new Chat("Msg5", dummyPlayer, dummyPlayer);
        chat6 = new Chat("Msg6", dummyPlayer, dummyPlayer);
    }

    /**
     * Tests if the chat is held within the chatHandler
     */
    @Test
    void testAddSingleChat() {
        handler.addChat(chat1);
        String history = handler.getFormattedHistory();
        assertTrue(history.contains("Msg1"));
    }

    /**
     * Test if the chatHandler removes the oldest message if it has more than 5 chats
     */
    @Test
     void testMaintainMaxHistorySize() {
        // Add 6 chats (1 more than max)
        handler.addChat(chat1);
        handler.addChat(chat2);
        handler.addChat(chat3);
        handler.addChat(chat4);
        handler.addChat(chat5);
        handler.addChat(chat6);

        String history = handler.getFormattedHistory();

        // Msg1 should be removed as it is the oldest message
        assertFalse(history.contains("Msg1"));
        // Newest chats should remain
        assertTrue(history.contains("Msg6"));
    }

    /**
     * Test checking if the format of chatHistory is correct
     */
    @Test
    void testFormatHistoryProperly() {
        handler.addChat(chat1);
        handler.addChat(chat2);

        String history = handler.getFormattedHistory();

        assertTrue(history.startsWith("Chat History:\n"));
        assertTrue(history.contains("Msg1"));
        assertTrue(history.contains("Msg2"));
        assertEquals(3, history.split("\n").length);
    }

    /**
     * Test checking if the handler has no chat objects inside.
     */
    @Test
    void testHandleEmptyHistory() {
        String history = handler.getFormattedHistory();
        assertEquals("Chat History:\n", history);
    }
}