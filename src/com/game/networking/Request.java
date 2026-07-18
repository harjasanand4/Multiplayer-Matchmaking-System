package com.game.networking;

/**
 * Request class.
 * Used for making different kinds of requests
 */
public abstract class Request {
    // Request fields
    private Player sender;
    private Player recipient;
    private RequestStatus status;

    /**
     * Constructor for Request
     * @param sender
     * @param recipient
     * @param status
     */
    public Request(Player sender, Player recipient, RequestStatus status) {
        this.sender = sender;
        this.recipient = recipient;
        this.status = status;
    }

    /**
     * Gets the sender player
     * @return sender
     */
    public Player getSender() {
        return sender;
    }

    /**
     * Gets the recipient player
     * @return recipient
     */
    public Player getRecipient() {
        return recipient;
    }

    /**
     * Gets the status of the request
     * @return
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets the sender
     * @param sender
     */
    public void setSender(Player sender) {
        this.sender = sender;
    }

    /**
     * Sets the recipient
     * @param recipient
     */
    public void setRecipient(Player recipient) {
        this.recipient = recipient;
    }

    /**
     * Sets the status for the request
     * @param status
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
