package com.game.networking;

/**
 * FriendRequest is a class that creates FriendRequest object that will be sent between players through the server.
 * It extends from the abstract class Request.
 */
public class FriendRequest extends Request{

    /**
     * Constructor for FriendRequest.
     * Inherits parameters from Request class.
     * @param sender
     * @param recipient
     */
    public FriendRequest(Player sender, Player recipient) {
        super(sender, recipient, RequestStatus.PENDING);
    }

    /**
     * Sets status of the request to ACCEPTED and adds the sender and recipient to each other's friends list
     */
    public void accept() {
        // Get the players
        Player sender = getSender();
        Player recipient = getRecipient();
        if (!(sender.getFriendList().contains(recipient) || recipient.getFriendList().contains(sender))){ // Added to make sure that friends don't duplicate
            if (getStatus() == RequestStatus.PENDING) {
                setStatus(RequestStatus.ACCEPTED); // Function from Request that sets status to ACCEPTED

                //Add them to the Players friend list
                sender.addFriend(recipient);
                recipient.addFriend(sender);

                recipient.removePendingRequest(this);
            } else {
                System.out.println("Request has already been accepted or declined");
            }
        } else {
            System.out.println("You're both already friends!");
        }
    }

    /**
     * Sets status of the request to REJECTED
     */
    public void decline(){
        if (getStatus() == RequestStatus.PENDING) {
            setStatus(RequestStatus.REJECTED); // Function from Request that sets status to REJECTED
            Player recipient = getRecipient();
            recipient.removePendingRequest(this);
        } else {
            System.out.println("Request has already been accepted or declined"); // I could split this message into two based on if the message has been already declined or rejected
        }
    }

}
