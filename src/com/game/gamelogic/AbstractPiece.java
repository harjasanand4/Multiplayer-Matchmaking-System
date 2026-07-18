package com.game.gamelogic;

public abstract class AbstractPiece {
    private int xPos, yPos;
    private String colour;

    public AbstractPiece(int xPos, int yPos, String colour) {
        this.yPos = yPos;
        this.xPos = xPos;
        this.colour = colour;
    }

    public int getxPos() {
        return xPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

    public String getColour() {
        return colour;
    }
}