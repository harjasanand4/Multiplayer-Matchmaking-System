package com.game.networking;



import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A game server stub that can host multiple games and handle client connections.
 * This server simulates a real online game server.
 * It handles client connections, hosts game sessions, and relays messages between clients.
 */
public class GameServer {
    private static final int DEFAULT_PORT = 8888;
    private ServerSocket serverSocket;
    private boolean running;
    private final ChatHandler chatHandler = new ChatHandler();
    private Map<String, Player> lobby = new ConcurrentHashMap<>();
    private Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private MatchmakingService matchmakingService;


    /**
     * Creates a GameServer
     *
     * @param port the port to broadcast the server on
     */
    public GameServer(int port) {

        try {
            this.serverSocket = new ServerSocket(port);
            this.connectedClients = new ConcurrentHashMap<>();  // map to store connected clients
            this.lobby = new ConcurrentHashMap<>(); // Map to store list of players
            this.matchmakingService = new MatchmakingService(this);
            this.running = true;

            System.out.println("Game server started on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to initialize server");
            e.printStackTrace();
        }
    }

    /**
     * Starts the server
     */
    public void start() {
        System.out.println("OMG started. Waiting for connections...");
        if(lobby == null){
            System.err.println("error, lobby is not initialized.");
            return;
        }
        while (running) {
            try {
                Socket playerSocket = serverSocket.accept(); // creates new socket for players
                Thread player = new Thread(() -> newPlayer(playerSocket)); //accept new players
                player.start();

                System.out.println("New connection accepted");
            } catch (Exception e) {
                System.err.println("failed to connect players");
            }
        }

    }

    /**
     * Adds a new Player to the server through the socket
     * @param playerSocket a client's socket
     */
    private void newPlayer(Socket playerSocket) {
        try {
            // Parse player data and create the client handler
            BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            String playerData = in.readLine();
            System.out.println("playerData received: " + playerData);
            String clientID = playerData.split(",")[3]; // extract ID
            ClientHandler clientHandler = new ClientHandler(playerSocket, this);
            clientHandler.setClientId(clientID);

            // Create a player object (assuming playerData contains necessary information)
            Player player = GameServer.parsePlayerData(playerData, clientHandler);

            if (clientID != null) {
                System.out.println("Player connected: " + clientID);
                lobby.put(clientID, player);
                connectedClients.put(clientID, clientHandler);

                // Start the ClientHandler in a new thread
                new Thread(clientHandler).start(); // This calls the 'run' method of ClientHandler
            } else {
                playerSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to connect new player: " + e.getMessage());
            try {
                playerSocket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Gets a player by their ID
     * @param playerId ID of the player to retrieve
     * @return Player object or null if not found
     */
    public Player getPlayerById(String playerId) {
        return lobby.get(playerId);
    }

    /**
     * Gets a list of all players in the lobby
     * @return List of players
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(lobby.values());
    }

    /**
     * Places a player in the random matchmaking queue
     * @param playerId ID of the player joining the queue
     * @param gameType Type of game to play
     */
    /**
     * Places a player in the random matchmaking queue
     * @param playerId ID of the player joining the queue
     * @param gameType Type of game to play
     */
    public void joinRandomMatchmaking(String playerId, GameType gameType) {
        Player player = getPlayerById(playerId);

        if (player != null && player.getState() == PlayerState.LOBBY) {
            // Player is in the lobby, they can join matchmaking
            matchmakingService.joinRandomQueue(player, gameType);
            player.setState(PlayerState.MATCHMAKING);  // Change player state to matchmaking
            System.out.println("Player " + playerId + " joined matchmaking for " + gameType);

            // Try to find a match immediately
            List<MatchmakingService.MatchedPair> matches = matchmakingService.processImmediateMatch();

            // Check if this player is in one of the matched pairs
            for (MatchmakingService.MatchedPair match : matches) {
                if (match.getPlayer1Id().equals(playerId) || match.getPlayer2Id().equals(playerId)) {
                    // Found a match for this player
                    Player player1 = getPlayerById(match.getPlayer1Id());
                    Player player2 = getPlayerById(match.getPlayer2Id());

                    if (player1 != null && player2 != null) {
                        matchPlayers(player1, player2, match.getGameType());
                        return; // We've matched this player, no need to continue
                    }
                }
            }
        } else {
            // Player is not in the lobby, can't join matchmaking
            System.out.println("Player " + playerId + " cannot join matchmaking unless in the lobby.");
        }
    }


    /**
     * Removes a player from all matchmaking queues
     * @param playerId ID of the player leaving the queue
     */
    public void leaveMatchmaking(String playerId) {
        Player player = getPlayerById(playerId);
        if (player != null && player.getState() == PlayerState.MATCHMAKING) {
            // Remove the player from the matchmaking queue
            matchmakingService.leaveQueue(playerId);
            player.setState(PlayerState.LOBBY);  // Change player state back to lobby
            System.out.println("Player " + playerId + " left matchmaking queue and returned to lobby");
        } else {
            // Player is not in matchmaking, handle accordingly
            System.out.println("Player " + playerId + " is not in matchmaking queue.");
        }
    }




    /**
     * Creates and starts a game session between two players.
     */
    public void matchPlayers(Player p1, Player p2, GameType gameType) {

        //remove players from lobby
        lobby.remove(p1.getID());
        lobby.remove(p2.getID());

        // Set players to IN_GAME state
        p1.setState(PlayerState.IN_GAME);
        p2.setState(PlayerState.IN_GAME);


        // Create the game session
        GameSession session = createGameSession(gameType, p1, p2);
        session.addPlayers(p1, p2);


        // Notify both players that a match has been found
        matchmakingService.notifyMatchFound(p1, p2, session.getSessionID(), gameType);
        matchmakingService.notifyMatchFound(p2, p1, session.getSessionID(), gameType);

        // Start the game
        ClientHandler handler1 = p1.getClientHandler();
        ClientHandler handler2 = p2.getClientHandler();

        if (handler1 != null && handler2 != null) {
            session.start(handler1, handler2);
            System.out.println("Game session " + session.getSessionID() + " created for " + p1.getUsername() + " and " + p2.getUsername());
        } else {
            System.err.println("Failed to start session: one or both ClientHandlers are null");
        }


    }

    /**
     * Creates a new GameSession to place players in
     * @param gameType the type of game they are playing
     * @param player1 the first player to be added
     * @param player2 the second player to be added
     * @return a GameSession with the correct players and gametype
     */
    public GameSession createGameSession(GameType gameType, Player player1, Player player2) {  //extract the game type and put it into a live game session
        String sessionId = UUID.randomUUID().toString();
        GameSession session = null;
        switch (gameType) {
            case TICTACTOE:
                session = new TicTacToeSession(sessionId,player1,player2);
                System.out.println(gameType + "session:" + sessionId + "between" + player1.getID() + "and" + player2.getID());
                break;
            case CHECKERS:
                session = new CheckerSession(sessionId,player1,player2);
                System.out.println(gameType + "session:" + sessionId + "between" + player1.getID() + "and" + player2.getID());
                break;
            case CONNECT4:
                session = new ConnectFourSession(sessionId,player1,player2);
                System.out.println(gameType + "session:" + sessionId + "between" + player1.getID() + "and" + player2.getID());
                break;
        }
        return session;
    }

    /**
     * Broadcasts a message to all players in the lobby
     * @param chat the message to send
     */
    public void broadcastMessage(Chat chat) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {   //sends message to everyone except for the sender
            if (!entry.getKey().equals(chat.getSender().getID())) {
                ClientHandler client = entry.getValue();
                client.sendMessage(chat.toString());
            }
        }
    }

    /**
     * Stores the chat into the gameServer, then sends it to each player individually
     * @param chat the message to send
     */
    public void broadcastPrivateMessage(Chat chat) {
        // Store in history
        chatHandler.addChat(chat);

        String chatHistory = getChatHistory();
        // Direct delivery each player
        chat.getSender().getClientHandler().displayChat(chatHistory);
        chat.getRecipient().getClientHandler().displayChat(chatHistory);
    }

    /**
     * Getter method to get chatHistory from oldest to youngest chats
     * @return String of chat history in current gameServer
     */
    public String getChatHistory() {
        return chatHandler.getFormattedHistory();
    }

    /**
     * Sends a friend request to another player by first getting the players from the connected clients hashmap. Once the players are found,
     * it creates a friend request object using the two players as parameters and that friend request to the recipients pending requests.
     * At the end a message is sent to the recipient that they got a friend request.
     * @param senderID the player ID of the sender
     * @param recipientID the player ID of the recipient
     */
    public void sendFriendRequest(String senderID, String recipientID) {
        ClientHandler recipientHandler = connectedClients.get(recipientID); // Gets the client handler for the recipient of the request

        if (recipientHandler != null) {
            // Get the sender and recipient for the request
            Player sender = connectedClients.get(senderID).getPlayer();
            Player recipient = recipientHandler.getPlayer();

            FriendRequest friendRequest = new FriendRequest(sender, recipient); // Create friend request using the two players

            recipient.addPendingRequest(friendRequest); // Add the friend request to the recipients pending requests

            recipientHandler.sendMessage("You got a friend request from "+ sender.getUsername()); // will be replaced with GUI message (ALSO ASK IF THESE MESSAGES EVEN WORK)
            // sender can also get a confirmation that the request went through here
        } else {
            System.out.println("Friend Request not sent!");
        }
    }

    /**
     * Accepts a friend request from another player by getting the player object of the recipient and going through their pending request until the senders ID is found.
     * Once the request from the sender is found, it calls a function to accept the request.
     * @param senderID the player ID of the sender
     * @param recipientID the player ID of the recipient
     */
    public void acceptFriendRequest(String senderID, String recipientID) {
        ClientHandler recipientHandler = connectedClients.get(recipientID);

        if (recipientHandler != null) {
            //Player sender = connectedClients.get(senderID).getPlayer();
            Player recipient = recipientHandler.getPlayer();

            ArrayList<Request> pendingRequests = recipient.getPendingRequests(); // Gets the pending request list from the recipient
            FriendRequest request = null;
            for (Request r : pendingRequests) { // Going through all the friend requests in the pending requests
                if (r.getSender().getID().equals(senderID) && r instanceof FriendRequest) { // Check if the request senders ID matches the actual senders ID
                    request = (FriendRequest) r;
                    break; // Once request is found it should break out immediately (for efficiency)
                }
            }

            if (request != null) {
                request.accept(); // Accepting the request that was found
                //recipient.removePendingRequest(request); // Once the request is accepted we should be removing the request
            } else {
                System.err.println("ERROR: There isn't a friend request from that user"); // this shouldn't really ever happen in the GUI implementation
            }
        } else {
            System.out.println("ERROR: ClientHander is null!");
        }
    }

    /**
     * Declines friend requests by getting the player object for the recipient, getting their pending request list and going through the list until
     * it finds a request that has a matching sender ID to the actual sender. Once the request is found the request is declined.
     * @param senderID the player ID of the sender
     * @param recipientID the player ID of the recipient
     */
    public void declineFriendRequest(String senderID, String recipientID) {
        ClientHandler recipientHandler = connectedClients.get(recipientID);

        if (recipientHandler != null) {
            //Player sender = connectedClients.get(senderID).getPlayer();
            Player recipient = recipientHandler.getPlayer();

            ArrayList<Request> pendingRequests = recipient.getPendingRequests(); // Gets the pending request list from the recipient
            FriendRequest request = null;
            for (Request r : pendingRequests) {
                if (r.getSender().getID().equals(senderID) && r instanceof FriendRequest) {
                    request = (FriendRequest) r;
                    break; // Once request is found it should break out immediately (for efficiency)
                }
            }

            if (request != null) {
                request.decline(); // Decline the request that was found
                //recipient.removePendingRequest(request); // Once the request is accepted we should be removing the request
            } else {
                System.err.println("ERROR: Friend request doesn't exist"); // This also shouldn't happen
            }
        }
    }

    /**
     * Sends an invite from one clienthandler to another.
     * @param senderID ID of sender
     * @param recipientID ID of recipient
     * @param gameType Type of game being requested to play
     */
    public void sendInvite(String senderID, String recipientID, GameType gameType) {
        ClientHandler recipientHandler = connectedClients.get(recipientID); // Gets the client handler for the recipient of the request

        if (recipientHandler != null) {
            // Get the sender and recipient for the request
            Player sender = connectedClients.get(senderID).getPlayer();
            Player recipient = recipientHandler.getPlayer();

            Request inviteRequest = new InviteRequest(sender, recipient, gameType); // Create friend request using the two players
            recipient.addPendingRequest(inviteRequest); // Add the friend request to the recipients pending requests
            // notify recipient
            recipientHandler.sendMessage("You got a Invite request from "+ sender.getUsername() + " to play " + gameType.toString()); // will be replaced with GUI message (ALSO ASK IF THESE MESSAGES EVEN WORK)
            // sender can also get a confirmation that the request went through here
        }
    }

    /**
     * Handles the invite and puts both players in a game if accepted
     * @param senderID The ID of the sender
     * @param recipientID The ID of the recipient
     * @param status status of the invite
     */
    public void handleInvite(String senderID, String recipientID, RequestStatus status) {
        ClientHandler recipientHandler = connectedClients.get(recipientID);
        if (recipientHandler != null) {
            Player sender = connectedClients.get(senderID).getPlayer();
            Player recipient = recipientHandler.getPlayer();

            ArrayList<Request> pendingRequests = recipient.getPendingRequests(); // Gets the pending request list from the recipient
            InviteRequest request = null;
            for (Request r : pendingRequests) {
                if (r.getSender().getID().equals(senderID) && r instanceof InviteRequest) {
                    request = (InviteRequest) r;
                    break; // Once request is found it should break out immediately (for efficiency)
                }
            }
            if (request != null && status.equals(RequestStatus.ACCEPTED)) {
                request.accept();
                matchPlayers(sender, recipient, request.getGameType());
            } else if (request != null && status.equals(RequestStatus.REJECTED)) {
                request.reject(); // Reject the invite request

            } else {
                System.err.println("ERROR: Friend request doesn't exist"); // This also shouldn't happen
            }
        }
    }
    /**
     * Stops the server from running
     */
    public void stop() {
        running = false;
    }

    /**
     * Receives a String input from a socket, and converts it to a player.
     *
     * @param playerData line from socket
     * @return a Player object
     */
    public static Player parsePlayerData(String playerData, ClientHandler clientHandler) {

        // ASSUMING DATA IS IN THE FORM "John,john@mail.com,imjohn,123"
        String[] parts = playerData.split(",");
        String username = parts[0];
        String email = parts[1];
        String password = parts[2];
        String id = parts[3];
        return new Player(id, username, email, password, clientHandler);
    }

//    public void handleDisconnect(Player player) {
//        connectedClients.remove(player.getID());
//        lobby.remove(player.getID());
//    }

    /**
     * SPECIFICALLY USED FOR TESTING
     * @return lobby
     */
    public Map<String, Player> getLobby() {
        return lobby;
    }

    /**
     * SPECIFICALLY USED FOR TESTING
     * @return connectedClients
     */
    public Map<String, ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    public boolean isRunning(){
    return running;}
}









