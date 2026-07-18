package com.game.networking;

import java.util.HashMap;

/**
 * ConnectionHealth tracks players networking connection data. This data is used either display the information
 * to the player or used to determine the strength of a connection.
 */
public class ConnectionHealth {
    // Holding latency Data in a hashmap using the player as the key (latency is measured in milliseconds)
    private HashMap<String, Integer> latencyData;
    // Holding packet loss data in a hashmap also using the player as the key (packet loss is represented as a percentage)
    private HashMap<String, Float> packetLossData;

    public ConnectionHealth() { // not 100% sure on this decision ****
        this.latencyData = new HashMap<>();
        this.packetLossData = new HashMap<>();
    }

    /**
     * Stores player latency
     * @param ID
     * @param latency
     */
    public void recordLatency(String ID, int latency) {
        latencyData.put(ID, latency);
    }

    /**
     * Stores player packet loss (as percentage)
     * @param ID
     * @param packetsLostPercentage
     */
    public void recordPacketLoss(String ID, float packetsLostPercentage){
        packetLossData.put(ID, packetsLostPercentage);
    }

    /**
     * Returns a players latency
     * @param ID
     * @return A players latency
     */
    public int getLatency(String ID){
        return latencyData.get(ID);
    }

    /**
     * Returns a player packet loss percentage
     * @param ID
     * @return A players packet loss
     */
    public float getPacketsLost(String ID){
        return packetLossData.get(ID);
    }

    /**
     * Checks a players latency and packetLoss and determines if the connection is strong or unstable
     * @param ID
     */
    public String checkConnectionHealth(String ID){ // this could actually return a boolean
        if (latencyData.containsKey(ID) && packetLossData.containsKey(ID)) {
            int latency = latencyData.get(ID);
            float packetLoss = packetLossData.get(ID);
            if (latency > 400 || packetLoss > 5) {
                return "WARNING: Network connection is unstable!";
            } else {
                return "Connection is strong!";
            }
        } else {
            return "Player ID doesn't exist";
        }
    }

}
