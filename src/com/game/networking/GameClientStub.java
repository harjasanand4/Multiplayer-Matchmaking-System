package com.game.networking;


public class GameClientStub {

    private String playerId;
    private String username;
    private Player player;
    private ClientHandler clientHandler;
    private GameServer gameServer;
    private GameSession gameSession;


    public GameClientStub(String playerId, String username, GameSession gamesession, ClientHandler clientHandler) {
        this.playerId = playerId;
        this.clientHandler = clientHandler;
        this.gameSession = gamesession;
        this.username = username;

        this.player = new Player(playerId,username, username+"@gmail.com","123456",clientHandler );

        clientHandler.setPlayer(player);//sets the connection with this player and the client socket

        System.out.println("Client started with player" + player.getID());
    }

    public void sendMove(String Move) {

        String moveUpdate = "MOVE:" + playerId+":" + Move;

        System.out.println("Player" + playerId + "sent move:" + Move);
        clientHandler.sendMessage(Move);
    }
}

