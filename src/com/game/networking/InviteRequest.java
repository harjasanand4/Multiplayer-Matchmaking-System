package com.game.networking;

public class InviteRequest extends Request {
    private GameType gameType;

    /**
     * Constructor of an Invite Request (requests that a game be played between two players.)
     *
     * @param sender Player that sends the request
     * @param recipient Player that receives the request
     * @param gameType Type of game played
     */
    public InviteRequest(Player sender, Player recipient, GameType gameType) {
        super(sender, recipient, RequestStatus.PENDING);
        this.gameType = gameType;
    }

    /**
     * Method that handles accepted invites. It changes the inbox of the player.
     */
    public void accept() {
        // Changes status of the game invite to accepted
        if (getStatus() == RequestStatus.PENDING) {
            setStatus(RequestStatus.ACCEPTED);
        } else {
            System.out.println("Invite request already accepted/declined");
        }
    }

    /**
     * Method that handles rejected invites, It changes the inbox of the player
     */
    public void reject() {
        // Changes status of the game invite to rejected
        if (getStatus() == RequestStatus.PENDING) {
            setStatus(RequestStatus.REJECTED);
        } else {
            System.out.println("Request has already been accepted or declined");
        }
    }

    /**
     * Getter for GameType
     * @return GameType of invite
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * Setter for GameType
     * @param gameType GameType of invite
     */
    public void setGameType(GameType gameType) {this.gameType = gameType;}
}
