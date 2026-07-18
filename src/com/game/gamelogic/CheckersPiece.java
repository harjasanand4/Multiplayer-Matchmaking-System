package com.game.gamelogic;

public class CheckersPiece extends AbstractPiece {
    private int xPos;
    private int yPos;
    private String colour;
    private boolean king;


    public CheckersPiece(int xPos, int yPos, String colour) {
        super(xPos, yPos, colour);
        this.xPos = super.getxPos();
        this.yPos = super.getyPos();
        this.colour = super.getColour();
        this.king = false;
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

    @Override
    public String getColour() {
        return this.colour;
    }

    public boolean isKing(){ return this.king;}

    public void setKing(boolean setKing){ this.king = setKing; }
}