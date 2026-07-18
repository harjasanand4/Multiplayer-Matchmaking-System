package com.game.networking;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Creates a Player that represents a client playing on the server.
 *
 *
 */
public class Player {
    private String ID;
    private String username;
    private String email;
    private String password;
    private PlayerState state;
    private ClientHandler clientHandler;
    private final ArrayList<Player> friendList = new ArrayList<>();
    private ArrayList<Request> pendingRequests = new ArrayList<>(); // Honestly could be shown in the players inbox


    /**
     * Constructor for Player Class (without clientHandler)
     *
     * @param ID
     * @param username
     * @param email

     */
    public Player(String ID, String username, String email, String password, ClientHandler clientHandler) {
        this.ID = ID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.clientHandler = clientHandler;
        this.state = PlayerState.LOBBY;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }


    /**
     * Getter for ID
     *
     * @return the player's unique ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Getter for Email
     *
     * @return the email used for the account creation
     */
    public String getEmail() {
        return email;
    }


    /**
     * Getter for Username
     *
     * @return the username of the account
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for Password
     * @return the password of the player
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter for ClientHandler
     * @return the clientHandler of the player
     */
    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    /**
     * Associates this player with their network connection
     */
    public void setClientHandler(ClientHandler handler) {
        this.clientHandler = handler;
    }


    /**
     * Sends message to specific player via clientHandler
     * @param message
     * @param recipient
     *
     */
    public void sendMessage(String message, Player recipient) {
        if (message == null || message.trim().isEmpty()) return;

        Chat chat = new Chat(message, this, recipient);
        if (this.clientHandler != null) {
            this.clientHandler.receiveChatMessage(chat); // Pass to ClientHandler
        }
    }

    /**
     * Adds a players friend to their friends list
     * @param friend
     */
    public void addFriend(Player friend) {
        friendList.add(friend);
    }

    /**
     * Removes a players friend from their friend list
     * @param friend
     */
    public void removeFriend(Player friend) {
        friendList.remove(friend);
    }

    /**
     * Gets the players friend list
     * @return friendList
     */
    public ArrayList<Player> getFriendList() {
        return friendList;
    }

    /**
     * Add a request to the pending requests
     * @param request The request to add
     */
    public void addPendingRequest(Request request) {
        pendingRequests.add(request);
    }

    /**
     * Remove a request from the pending requests
     * @param request the request to add
     */
    public void removePendingRequest(Request request) {
        pendingRequests.remove(request);
    }

    /**
     * Gets the list of pending requests
     * @return an arraylist of all requests, friend/invite
     */
    public ArrayList<Request> getPendingRequests() {
        return pendingRequests;
    }

}
