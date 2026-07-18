package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the entire com.game.networking.chat class
 */
public class ChatTest {
    private Player sender;
    private Player recipient;

    /**
     * Sets up two players to interact using chat
     */
    @BeforeEach
    void setup() {
        sender = new Player("654321", "Alice", "alice@gmail.com", "password123", null);
        recipient = new Player("123456", "Bob", "bob@gmail.com", "password456", null);
    }

    /**
     * Test regarding if chat objects are made correctly
     */
    @Test
    void testValidMessage() {
        Chat chat = new Chat("Hello Bob!", sender, recipient);
        assertEquals("Hello Bob!", chat.getMessageContent());
        assertEquals(sender, chat.getSender());
        assertEquals(recipient, chat.getRecipient());
        assertNotNull(chat.getTimeStamp());
    }

    /**
     * Test checking when a message is empty
     */
    @Test
    void testEmptyMessage() {
        Chat chat = new Chat(null, sender, recipient);
        assertEquals("", chat.getMessageContent());
    }

    /**
     * Test when a message is over 200 characters
     */
    @Test
    void testMessageTooLong() {
        String longMessage = "";
        for(int i = 0; i < 201; i++){
            longMessage += "X";
        }
        Chat chat = new Chat(longMessage, sender, recipient);
        assertEquals(200, chat.getMessageContent().length());
    }

    /**
     * Test if the timestamp formatting is correct
     */
    @Test
    void testTimestampFormatting() {
        Chat chat = new Chat("Test", sender, recipient);

        //The following code is from ChatGPT as I did not know how to check for the specific time the tests are ran (volatile).
        assertTrue(chat.getFormattedTimeStamp().matches(
                "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    /**
     * Test if the toString method correctly contains all elements
     */
    @Test
    void testToStringFormat() {
        Chat chat = new Chat("Test message", sender, recipient);
        String result = chat.toString();
        assertTrue(result.contains(chat.getFormattedTimeStamp()));
        assertTrue(result.contains(sender.getUsername()));
        assertTrue(result.contains("Test message"));
        assertTrue(result.equals(chat.getFormattedTimeStamp() + " - " + sender.getUsername() + ": " + chat.getMessageContent()));
    }


}