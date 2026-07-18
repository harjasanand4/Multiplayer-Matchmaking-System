package com.game.gui;

import java.util.Random;

/**
 * The NetworkUtils class provides a centralized location for handling network-related
 * operations in the Online Multiplayer Board Game Platform. This includes sending moves,
 * draw requests, forfeit notifications, rematch requests, and chat messages to the server.
 * It also provides methods to check for responses from the server, such as draw acceptance
 * or rematch acceptance.
 *
 * Currently, all methods are simulated for testing purposes. The Networking team should
 * replace these simulations with actual network calls (e.g., using sockets, HTTP requests,
 * or a WebSocket connection) to communicate with the game server.
 *
 * Usage:
 * - This class should be used by game controllers (e.g., ConnectFourController) to handle
 *   all network interactions in online match types (CASUAL_ONLINE and RANKED).
 * - Methods are static for ease of use without instantiation.
 *
 * Important Notes:
 * - The `isConnected` flag simulates network connectivity. In a real implementation,
 *   this should be replaced with actual connection status checks.
 * - Random delays are used to simulate network latency. Remove or adjust these in the
 *   final implementation.
 * - The methods that return simulated responses (e.g., isDrawAccepted, isRematchAccepted)
 *   should be replaced with actual server response handlers.
 */
public class NetworkUtils {
    private static final Random random = new Random(); // Used for simulating responses and delays
    private static boolean isConnected = true; // Simulated connection status; true means connected

    /**
     * Sends a player's move to the server.
     *
     * @param column The column where the player placed their piece (e.g., in Connect Four).
     * @param player The name of the player making the move.
     *
     * What it does:
     * - Currently, it prints a message and simulates a delay to mimic sending a move.
     * - If not "connected," it reports a network error and exits.
     *
     * In the final implementation:
     * - This method should serialize the move data (e.g., column and player info) into a format
     *   like JSON and send it to the server via a network protocol (e.g., WebSocket).
     * - It should handle network errors (e.g., timeouts) and notify the player if the move fails.
     * - The server will validate the move and broadcast it to the opponent.
     *
     * Used by:
     * - Game controllers when a player makes a move in an online match.
     */
    public static void sendMove(int column, String player) {
        if (!isConnected) {
            System.out.println("Network error: Not connected to server.");
            return;
        }
        System.out.println("Sending move to server: Player " + player + " placed piece in column " + column);
        // Simulate network delay (500ms) to mimic real-world latency; remove in final version
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: Networking team to implement actual move sending logic
    }

    /**
     * Sends a draw request to the server.
     *
     * @param proposingPlayer The name of the player proposing the draw.
     *
     * What it does:
     * - Prints a message and simulates a delay to mimic sending a draw request.
     * - Checks connection status first.
     *
     * In the final implementation:
     * - This should send a draw request to the server, which notifies the opponent.
     * - The server waits for the opponent's response (accept/reject).
     *
     * Used by:
     * - Game controllers when a player clicks the "Propose Draw" button in an online game.
     */
    public static void sendDrawRequest(String proposingPlayer) {
        if (!isConnected) {
            System.out.println("Network error: Not connected to server.");
            return;
        }
        System.out.println("Sending draw request from " + proposingPlayer);
        // Simulate network delay (500ms); replace with actual network call
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: Networking team to implement actual draw request logic
    }

    /**
     * Checks if the opponent has accepted the draw request.
     *
     * @return True if the opponent accepted the draw, false otherwise.
     *
     * What it does:
     * - Currently returns a random boolean to simulate the opponent's response.
     * - Returns false if not "connected."
     *
     * In the final implementation:
     * - This should query the server for the opponent's response to the draw request.
     * - Could use a callback (e.g., WebSocket listener) or poll the server periodically.
     *
     * Used by:
     * - Game controllers to determine if the game ends in a draw after a request is sent.
     */
    public static boolean isDrawAccepted() {
        if (!isConnected) return false;
        // Simulate opponent response with 50/50 chance; replace with server data
        return random.nextBoolean();
    }

    /**
     * Sends a forfeit notification to the server.
     *
     * @param forfeitingPlayer The name of the player forfeiting the game.
     *
     * What it does:
     * - Prints a message and simulates a delay to mimic sending a forfeit.
     * - Checks connection status first.
     *
     * In the final implementation:
     * - This should notify the server that the player has forfeited.
     * - The server ends the game and informs the opponent of the victory.
     *
     * Used by:
     * - Game controllers when a player clicks "Forfeit" in an online match.
     */
    public static void sendForfeit(String forfeitingPlayer) {
        if (!isConnected) {
            System.out.println("Network error: Not connected to server.");
            return;
        }
        System.out.println("Sending forfeit from " + forfeitingPlayer);
        // Simulate network delay (500ms); replace with actual network call
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: Networking team to implement actual forfeit logic
    }

    /**
     * Sends a rematch request to the server.
     *
     * @param requestingPlayer The name of the player requesting the rematch.
     *
     * What it does:
     * - Prints a message and simulates a delay to mimic sending a rematch request.
     * - Checks connection status first.
     *
     * In the final implementation:
     * - This should send a rematch request to the server, which notifies the opponent.
     * - The server waits for the opponent's response (accept/reject).
     *
     * Used by:
     * - Game controllers when a player clicks "Rematch" after a game ends.
     */
    public static void sendRematchRequest(String requestingPlayer) {
        if (!isConnected) {
            System.out.println("Network error: Not connected to server.");
            return;
        }
        System.out.println("Sending rematch request from " + requestingPlayer);
        // Simulate network delay (500ms); replace with actual network call
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: Networking team to implement actual rematch request logic
    }

    /**
     * Checks if the opponent has accepted the rematch request.
     *
     * @return True if the opponent accepted the rematch, false otherwise.
     *
     * What it does:
     * - Returns a random boolean to simulate the opponent's response.
     * - Returns false if not "connected."
     *
     * In the final implementation:
     * - This should check the server's response to the rematch request.
     * - Could use a callback or poll the server for the opponent's decision.
     *
     * Used by:
     * - Game controllers to determine if a new game starts after a rematch request.
     */
    public static boolean isRematchAccepted() {
        if (!isConnected) return false;
        // Simulate opponent response with 50/50 chance; replace with server data
        return random.nextBoolean();
    }

    /**
     * Sends a chat message to the server.
     *
     * @param sender The name of the player sending the message.
     * @param message The chat message content.
     *
     * What it does:
     * - Prints the message and simulates a shorter delay (200ms) to mimic sending a chat.
     * - Checks connection status first.
     *
     * In the final implementation:
     * - This should send the message to the server, which broadcasts it to the opponent.
     * - Handle errors (e.g., message not sent) and notify the player.
     *
     * Used by:
     * - Game controllers when a player sends a chat message during an online match.
     */
    public static void sendChatMessage(String sender, String message) {
        if (!isConnected) {
            System.out.println("Network error: Not connected to server.");
            return;
        }
        System.out.println("Sending chat message from " + sender + ": " + message);
        // Simulate network delay (200ms); replace with actual network call
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: Networking team to implement actual chat message logic
    }

    /**
     * Retrieves network details for display in the game UI.
     *
     * @return A string containing network information (e.g., ping, server address, status).
     *
     * What it does:
     * - Returns simulated network stats (random ping, fake server address) if connected.
     * - Returns "Disconnected" if not connected.
     *
     * In the final implementation:
     * - This should fetch real-time stats (e.g., ping, server IP) from the network connection.
     * - Useful for debugging or informing the player about their connection quality.
     *
     * Used by:
     * - Game UI when the player clicks the network icon to view connection details.
     */
    public static String getNetworkDetails() {
        if (!isConnected) {
            return "Status: Disconnected";
        }
        // Simulate ping between 30-100ms; replace with real data
        int ping = 30 + random.nextInt(70);
        return "Ping: " + ping + "ms\nServer: game.server.com\nStatus: Connected";
    }

    /**
     * Sets the connection status (for simulation purposes).
     *
     * @param connected True if connected, false otherwise.
     *
     * What it does:
     * - Updates the `isConnected` flag for testing different scenarios.
     *
     * In the final implementation:
     * - This can be removed or replaced with real connection management logic (e.g., checking
     *   WebSocket status or socket connectivity).
     *
     * Used by:
     * - Testing code to simulate connection/disconnection; not needed in production.
     */
    public static void setConnected(boolean connected) {
        isConnected = connected;
    }


}