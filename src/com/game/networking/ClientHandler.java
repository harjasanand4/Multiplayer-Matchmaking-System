package com.game.networking;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    private Player player;
    private GameServer gameServer;
    private String clientID;

    private boolean isRunning;
    private ConnectionState connectionState;
    private int latencyMS;
    private int packetsSent;
    private int packetsReceived;
    private InetSocketAddress originalAddress;
    private ConnectionHealth clientConnectionHealth;
    List<String> messagesSent;

    public ClientHandler(Socket clientSocket, GameServer gameServer) {
        this.clientSocket = clientSocket;
        this.gameServer = gameServer;
        this.connectionState = ConnectionState.CONNECTED;
        this.isRunning = true;
        this.messagesSent = new ArrayList<>();
        if(clientSocket != null){
            this.originalAddress = new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort());
        }
    }

    /**
     * Creates a connection between player and the client socket
     * @param player
     */
    public void setPlayer(Player player){
        this.player = player;
        player.setClientHandler(this);
    }

    /**
     * Getter to get player connected to client socket
     * @return Player associated with clientHandler
     */
    public Player getPlayer(){
        return player;
    }

    /**
     * Disconnects player by changing connection state
     */
    public synchronized void disconnect() {
        if (connectionState == ConnectionState.DISCONNECTED) {
            return;
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting " + clientID + ": " + e.getMessage());
        } finally {
            connectionState = ConnectionState.DISCONNECTED;
            isRunning = false;
        }
    }

    /**
     * Reconnects player by changing connection state
     */
    public synchronized void reconnect() {
        if (connectionState != ConnectionState.DISCONNECTED) {
            return;
        }
        connectionState = ConnectionState.RECONNECTING;
        long timer = System.currentTimeMillis(); // Timer using milliseconds
        if (originalAddress == null) {
            connectionState = ConnectionState.CONNECTED;
            isRunning = true;
            return;
        }

        try {
            while ((System.currentTimeMillis() - timer) < 60_000) { // 60-second window
                Thread.sleep(3000); // Wait 3 seconds between attempts

                try {
                    Socket newSocket = new Socket();
                    newSocket.connect(originalAddress, 3000); // 3-second connection timeout

                    // Successfully reconnection changes states of current clientHandler
                    synchronized (this) {
                        this.clientSocket = newSocket;
                        connectionState = ConnectionState.CONNECTED;
                        isRunning = true;
                        System.out.println("Reconnected successfully!");
                        return;
                    }
                } catch (IOException e) {
                    System.err.println("Reconnection attempt failed: " + e.getMessage());
                }
            }
            // All attempts failed
            System.err.println("Reconnection window expired.");
            connectionState = ConnectionState.DISCONNECTED;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connectionState = ConnectionState.DISCONNECTED;
        }
    }

    /**
     * Checks if the player is currently connected
     * @return Boolean if the connectionState is CONNECTED
     */
    public synchronized boolean isConnected(){
        return connectionState == ConnectionState.CONNECTED && !clientSocket.isClosed() && clientSocket != null;
    }

    /**
     * Getter for running status
     * @return if the client is running and connected
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Getter for connection state
     * @return ConnectionState of current clientConnection
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Sets new clientId to match parameters
     * @param Id
     */
    public void setClientId(String Id) {
        clientID = Id;
    }

    /**
     * Sends message to client
     * @param message
     */
    public void sendMessage(String message) {
        if (message != null) {
            System.out.println(message);
            messagesSent.add(message);

        }
    }
    /**
    * returns of a list of messages
    *
     */

        public List<String> getMessagesSent () {
            return messagesSent;

    }

        /**
         * Receives the initial message from player then sends it to gameServer
         * @param chat
         */
        public void receiveChatMessage (Chat chat){
            gameServer.broadcastPrivateMessage(chat);
        }

        /**
         * Displays a chat in the player's UI
         */
        public void displayChat (String message){
            sendMessage(message);
        }

        /**
         * Calls disconnect which closes the socket and changes the connection state of the client handler
         */
        public void stop () {
            disconnect();
        }

        /**
         * Essentially starts the client handler for a player. In this function, it is constantly sending "pings" and looking for "pongs" to record
         * latency and packet loss percentage. Since the server will be running on a local machine latency and packet loss won't every be too high.
         * But this code does properly record it so if the server was ever put online it'll work as intended.
         */
        @Override
        public void run () {
            clientConnectionHealth = new ConnectionHealth();
            // Gets latency and packet information between the connection of the client and the server
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                while (!clientSocket.isClosed()) {
                    packetsSent++; // Sending a packet so should increment by 1
                    long startTime = System.nanoTime(); // Start a timer so latency between sending and receiving a ping can be calculated

                    out.println("PING");  // Sends a "ping" to the client
                    out.flush();  // This ensures its done right away

                    Thread.sleep(1000); // Used this to test if latency changes (it does)

                    String response = in.readLine();
                    if ("PONG".equals(response)) {
                        packetsReceived++; // We received a packet so we increment the counter
                        long endTime = System.nanoTime();// We record the end time once the packet is received

                        // Now the latency will be calculated
                        latencyMS = (int) ((endTime - startTime) / 1_000_000); // Making sure to divide by 1 000 000 to convert the nanoseconds to milliseconds
                    }

                    // Going to check connection health every 5 packets (don't want to over check)
                    if (packetsSent % 5 == 0) {
                        // Calculate packet loss here (as a percentage)
                        int packetLoss = (int) (((double) (packetsSent - packetsReceived) / packetsSent) * 100);
                        // Record our data
                        clientConnectionHealth.recordPacketLoss(clientID, packetLoss);
                        clientConnectionHealth.recordLatency(clientID, latencyMS);
                        monitorConnectionHealth(clientConnectionHealth);
                    }

                    Thread.sleep(1000); // wait one second before pings (This prevents over pinging (IMPORTANT))
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Connection error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {
                }
            }
        }

        /**
         * Function displays data gathered from the "ping" system and also determines if the connection is healthy or not.
         * @param clientConnectionHealth
         */
        public void monitorConnectionHealth (ConnectionHealth clientConnectionHealth){
            // This is where I should ask GUI if we got a space for this to display for the user. It could stay in the console, but it would be nice to display

            String status = clientConnectionHealth.checkConnectionHealth(clientID);

            // using \r here prevents the ping taking up the entire console. Only the values will change
            System.out.print("\rLatency:" + clientConnectionHealth.getLatency(clientID) + "ms");
            System.out.print(" | Packet Loss:" + clientConnectionHealth.getPacketsLost(clientID) + "%");
            System.out.print(" | " + status);
        }

        /**
         * Calls a function from the game server to send a friend request to a certain player using their ID
         * @param recipientID
         */
        public void sendFriendRequest (String recipientID){
            // Getting the player from the handler and the server
            Player player = getPlayer();
            GameServer server = gameServer;

            // Send a friend request to the recipient
            server.sendFriendRequest(player.getID(), recipientID);
            sendMessage("Friend request sent to " + recipientID); // Make in GUI
        }

        /**
         * Calls a function from the game server to accept the friend request of a certain player using their ID
         * @param recipientID
         */
        public void acceptFriendRequest (String senderID){
            // 'player' is the one who is accepting the friend request (the recipient)
            Player player = getPlayer();
            GameServer server = gameServer;

            // Now pass the sender's ID first and the recipient's (player's) ID second.
            server.acceptFriendRequest(senderID, player.getID());
            sendMessage("Friend request accepted from " + senderID);
        }


        /**
         * Calls a function from the game server that allows the player to decline a certain friend request that they have reviewed (also based on ID)
         * @param recipientID
         */
        public void declineFriendRequest (String senderID){
            // 'player' is the one who is declining the request (the recipient)
            Player player = getPlayer();
            GameServer server = gameServer;

            // Now pass sender's ID first and recipient's (player's) ID second.
            server.declineFriendRequest(senderID, player.getID());
            sendMessage("Friend request declined from " + senderID);
        }

        /**
         * Calls a function from the game server that allows a player to send a Game Invite
         * @param recipientID the ID of the recipient of the invite
         * @param type the type of game being played
         */
        public void sendInvite (String recipientID, GameType type){
            // Getting the player from the handler and the server
            Player player = getPlayer();
            GameServer server = gameServer;

            // Send a friend request to the recipient
            server.sendInvite(player.getID(), recipientID, type);
            sendMessage("Invite sent to " + recipientID); // Make in GUI
        }

        /**
         * Calls a function from the game server that allows a player to accept a Game Invite
         * @param recipientID ID of the recipient player
         */
        public void acceptInvite (String recipientID){
            // Getting the player from the handler and the server
            Player player = getPlayer();
            GameServer server = gameServer;

            // Accept the game invite
            server.handleInvite(player.getID(), recipientID, RequestStatus.ACCEPTED);
            sendMessage("Invite accepted from " + recipientID); // Make in GUI
        }

        /**
         * Calls a function from the game server that allows a player to reject a Game Invite
         * @param recipientID ID of the recipient player
         */
        public void declineInvite (String recipientID){
            // Getting the player from the handler and the server
            Player player = getPlayer();
            GameServer server = gameServer;

            // Decline the game invite
            server.handleInvite(player.getID(), recipientID, RequestStatus.REJECTED);
            sendMessage("Invite declined from " + recipientID); // Make in GUI
        }

        /**
         * SPECIFICALLY USED FOR TESTING
         * @return String of ClientID
         */
        public String getClientId () {
            return clientID;
        }


}


