package com.game.networking;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Stub for Database. Will be replaced by actual database by the OMG Database team
 * @author Jed Cravalho
 * @email: jed.cravalho@ucalgary.ca
 */
public class DatabaseStub {
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, ArrayList<GameState>> gameSessions = new ConcurrentHashMap<>();

    /**
     * A constructor for a new Database
     */
    public DatabaseStub() {
        this.players = new ConcurrentHashMap<>();
        this.gameSessions = new ConcurrentHashMap<>();
    }

    /**
     * Inserts a player into the database
     * @param id Player ID
     * @param player Player to add
     */
    public void insertPlayer(String id, Player player) {
        this.players.put(id, player);
    }

    /**
     * Inserts a GameState in the database
     * @param id Session ID
     * @param gameState GameState to add
     */
    public void insertGameState(String id, GameState gameState) {
        // check if GameSession already has a game. If no, create new GameState arrayList
        if (!this.gameSessions.containsKey(id)) {
            this.gameSessions.put(id, new ArrayList<>());
            gameSessions.get(id).add(gameState);
        } else {
            // add GameState
            this.gameSessions.get(id).add(gameState);
        }
    }
    /**
     * Retrieves a player from the database
     * @param id Player ID
     * @return The Player object
     */
    public Player retrievePlayer(String id) {
        return this.players.get(id);
    }

    /**
     * Retrieves the latest GameState from the database
     * @param id the GameSession ID
     * @return the latest GameState
     */
    public GameState retrieveGameState(String id) {
        if (!this.gameSessions.containsKey(id)) {
            return null;
        }
        // get the last GameState added to the GameSession
        return this.gameSessions.get(id).get(gameSessions.get(id).size() - 1);
    }

}
