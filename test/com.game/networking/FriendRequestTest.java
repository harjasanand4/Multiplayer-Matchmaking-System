package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class used for testing FriendRequest tests
 */
public class FriendRequestTest {
    Player sender;
    Player recipient;
    ClientHandler clientHandler1;
    ClientHandler clientHandler2;
    Socket clientSocket1;
    Socket clientSocket2;
    GameServer server;
    FriendRequest friendRequest;

    /**
     * Setup all fields for testing FriendRequest class
     */
    @BeforeEach
    void setup() {
        server = new GameServer(8888);
        clientSocket1 = new Socket();
        clientSocket2 = new Socket();
        clientHandler1 = new ClientHandler(clientSocket1, server);
        clientHandler2 = new ClientHandler(clientSocket2, server);
        sender = new Player("0000", "THE_GOAT", "Goat@mail.com", "123", clientHandler1);
        recipient = new Player("1111", "THE_Moat", "Moat@mail.com", "312", clientHandler2);
        friendRequest = new FriendRequest(sender, recipient);
    }

    /**
     * Tests if accepting a friend request changes the status on the invite to ACCEPTED
     */
    @Test
    void acceptRequestStatusChangeTest() {
        friendRequest.accept();
        assertEquals(RequestStatus.ACCEPTED,friendRequest.getStatus());
    }

    /**
     * Tests if declining friend request changes the status on the invite to DECLINED
     */
    @Test
    void declineRequestStatusChangeTest() {
        friendRequest.decline();
        assertEquals(RequestStatus.REJECTED,friendRequest.getStatus());
    }

    /**
     * Test if accepting a friend request added the two players to their friendlist
     */
    @Test
    void acceptRequestFriendListUpdatedTest() {
        friendRequest.accept();
        assertTrue(recipient.getFriendList().contains(sender));
        assertTrue(sender.getFriendList().contains(recipient));
    }

    /**
     * Test if accepting a friend request removes the sender from the recipients pending requests
     */
    @Test
    void acceptRequestPendingRequestListUpdatedTest() {
        friendRequest.accept();
        assertTrue(!recipient.getPendingRequests().contains(sender));
        assertTrue(!sender.getPendingRequests().contains(recipient));
    }

    /**
     * Tests if declining friend request removes sender from recipients pending requests
     */
    @Test
    void declineRequestPendingRequestListUpdatedTest() {
        friendRequest.decline();
        assertTrue(!recipient.getPendingRequests().contains(sender));
        assertTrue(!sender.getPendingRequests().contains(recipient));
    }

    /**
     * Tests if accepting two identical friend requests will duplicate the friend in the friend list
     */
    @Test
    void checkForDuplicateFriendsTest() {
        friendRequest.accept();
        FriendRequest friendRequest2 = new FriendRequest(sender,recipient);
        friendRequest2.accept();

        int count = 0;

        for (Player friend : recipient.getFriendList()) {
            if (friend.equals(sender)) {
                count++;
            }
        }

        assertEquals(1,count); // The friend should only show up in the friend list once. If any more than that then the function is not working as intended
    }
    /**
     * Tests getters and setters for FriendRequest object
     */
    @Test
    void getterandsettersTest() {
        // Getters
        assertEquals(sender, friendRequest.getSender());
        assertEquals(recipient, friendRequest.getRecipient());
        assertEquals(RequestStatus.PENDING, friendRequest.getStatus());

        // Setters
        Player tester = new Player(null, null, null, null, null);
        friendRequest.setSender(tester);
        assertEquals(tester, friendRequest.getSender());
        friendRequest.setRecipient(tester);
        assertEquals(tester, friendRequest.getRecipient());
        friendRequest.setStatus(RequestStatus.REJECTED);
        assertEquals(RequestStatus.REJECTED, friendRequest.getStatus());
    }

}
