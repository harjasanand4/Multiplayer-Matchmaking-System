package com.game.gamelogic;

public class Connect4Piece extends AbstractPiece{

    private int xPos;
    private int yPos;
    private String colour;

    public Connect4Piece(int xPos, int yPos, String colour) {
        super(xPos, yPos, colour);
        this.xPos = super.getxPos();
        this.yPos = super.getyPos();
        this.colour = super.getColour();
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

}
