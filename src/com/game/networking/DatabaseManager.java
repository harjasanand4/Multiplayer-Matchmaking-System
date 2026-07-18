package com.game.networking;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages data sent to and from the database.
 *
 */
public class DatabaseManager {

    private DatabaseStub db;
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, ArrayList<GameState>> gameSessions = new ConcurrentHashMap<>();
    public DatabaseManager(DatabaseStub db) {
        this.db = db;
    }


    /**
     * Updates or saves current gameStates
     *
     * @param ID Session ID
     * @param gameState GameState to save
     */
    public void saveGameState(String ID, GameState gameState) {
        db.insertGameState(ID, gameState);
    }

    /**
     * Gets a list of gameStates from the file
     *
     * @param sessionId ID of GameSession
     * @return the requested GameState
     */
    public ArrayList<GameState> getGameStates(String sessionId) {
        return gameSessions.get(sessionId);
    }

    /**
     * Gets latest GameState from the server
     * @param sessionId the GameSession ID
     * @return the GameState to be returned
     */
    public GameState getLatestGameState(String sessionId) {
        return db.retrieveGameState(sessionId);
    }

    /**
     * Saves a player to the database
     * @param player Player to save
     */
    public synchronized void savePlayer(Player player) {
        db.insertPlayer(player.getID(), player);
    }

}




