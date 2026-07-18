package com.game.gamelogic;

public class CheckersBoard extends AbstractBoard {


    /**
     * The constructor for CheckersBoard that takes in
     * 8 rows and 8 columns(the standard size for a checkerboard)
     *
     * @param rows number of rows for the checkerboard
     * @param columns number of columns for the checkerboard
     */
    public CheckersBoard(int rows, int columns) {
        super(rows, columns);
    }


    /**
     * setPiece function take in a checker piece
     * and assign it onto the board
     * @param piece the checker piece that will be place onto the board
     */
    public void setPiece(CheckersPiece piece){
        board[piece.getxPos()][piece.getyPos()] = piece;
    }

    /**
     * removePiece function take in a checker piece and remove it from the board
     * This functions is mainly use when a player capture a piece
     * @param piece the checker piece that will be removed from the board
     */
    public void removePiece(CheckersPiece piece){
        board[piece.getxPos()][piece.getyPos()] = null;
    }

    /**
     * getPiece function take in the x and y coordinate of the checkerboard
     * and return the information of that checkerPiece
     *
     * @param x the x coordination of that checker piece
     * @param y the y coordination of that checker piece
     * @return the information that stored in that checker piece
     */
    public CheckersPiece getPiece(int x, int y){
        return (CheckersPiece) board[x][y];
    }


    /**
     * initializeBoard function place the checker piece to
     * it initial position
     */
    public void initializeBoard(){

        //Initialize white pieces in row 0 and row 2
        for(int i = 1; i < 8; i+=2){
            setPiece(new CheckersPiece(0, i, "w"));
            setPiece(new CheckersPiece(2, i, "w"));
        }

        //Initialize white pieces in row 1
        for(int i = 0; i < 8; i+=2){
            setPiece(new CheckersPiece(1, i, "w"));
        }

        //Initialize black pieces in row 5 & 7
        for(int i = 0; i < 8; i+=2){
            setPiece(new CheckersPiece(5, i, "r"));
            setPiece(new CheckersPiece(7, i, "r"));
        }

        //Initialize black pieces in row 6
        for(int i = 1; i < 8; i+=2){
            setPiece(new CheckersPiece(6, i, "r"));
        }
    }


    /**
     * canPlay function check if a checker piece is movable at it new x,y position
     * @param x the new x position of the checker piece
     * @param y the new y position of the checker piece
     * @return true if the piece can move to the new x,y position, otherwise false
     */
    @Override
    public boolean canPlay(int x, int y) {
        boolean isEmpty = false;
        if(0 <= x && x < board.length && 0 <= y && y < board[0].length) {
            //Check if the new x,y position is empty
            if (board[x][y] == null) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    /**
     *
     * checkForWinner function check if either player has won in a match
     * Case 1: The function will check if there are any black or white pieces left
     * on the board, and announce the winner if there are no black or white piece left
     *
     * Case 2: The function will also check all the possible moves(such as able to capture or move)
     * of black and white pieces. If one side run out of moves the winner will be announced
     *
     * Case 3: After 40 moves without a single capture, the game will result in a draw
     * @return "none" when there is no winner(game continued), "White" for white won, "Black" for black won
     */
    public String checkForWinner() {
        String winner = "none";


        int black_pieces = 0; //Count the number of black pieces left on the board
        int white_pieces = 0; //Count the number of white pieces left on the board

        int black_moves = 0; //Count the number of available move for black pieces
        int white_moves = 0; //Count the number of available move for white pieces

        int noCapture;


        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[0].length; j++){
                //Check if there is a piece at the current position
                if(board[i][j] != null) {
                    //Increment number of white pieces if the piece labeled "w"
                    if (board[i][j].getColour().equals("w")) {
                        white_pieces += 1;
                        //Calculate all the possible moves of that white piece
                        white_moves += count_moves((CheckersPiece) board[i][j]);

                    //Increment number of black pieces if the piece labeled "b"
                    } else if (board[i][j].getColour().equals("r")) {
                        black_pieces += 1;
                        //Calculate all the possible moves of that black piece
                        black_moves += count_moves((CheckersPiece) board[i][j]);
                    }
                }
            }
        }

        //Announce white win if there are no black piece left
        if(black_pieces == 0){
            winner = "White";
            return winner;
        }

        //Announce black win if there are no white piece left
        if(white_pieces == 0){
            winner = "Red";
            return winner;
        }


        if(black_moves == 0 && white_moves == 0){
            winner = "Draw";
            return winner;

        //Announce black win if white run out of move
        } else if (black_moves == 0) {
            winner = "White";
            return winner;

        //Announce white win if black run out of move
        } else if (white_moves == 0) {
            winner = "Red";
            return winner;
        }

        CheckersLogic logic = new CheckersLogic("w", "r");

        //Get the number of moves with no capture
        noCapture = logic.getNoCaptures();

        //Announce a draw after 40 moves without a capture
        if (noCapture >= 40){
            winner = "Draw";
            return winner;
        }

        return winner;
    }

    /**
     * count_moves function count the available moves of the current Piece
     * Check if the diagonal movement of a piece is empty and inbound
     * Also check if a piece is able to capture; as it is a part of a move in checker
     * @param piece the current piece that we are checking for available moves
     * @return the number of possible moves that that Piece had
     */
    public int count_moves(CheckersPiece piece){
        int moves = 0;
        int xPos = piece.getxPos();
        int yPos = piece.getyPos();
        String type = piece.getColour();
        boolean king = piece.isKing();

        //White Diagonal Down to the left
        if(type.equals("w") && king == false){

            int newX = xPos + 1;
            int newY = yPos - 1;


            //Check if you can play(if it empty or inbound) there
            if(canPlay(newX,newY)){
                moves += 1;
            }
        }

        //White Diagonal Down to the right
        if(type.equals("w") && king == false){
            int newX = xPos + 1;
            int newY = yPos + 1;

            if(canPlay(newX,newY)){
                moves += 1;
            }
        }

        //Black Diagonal Up to the left
        if(type.equals("r") && king == false){
            int newX = xPos - 1;
            int newY = yPos - 1;
            if(canPlay(newX,newY)){
                moves += 1;
            }
        }

        //Black Diagonal Up to the right
        if(type.equals("r") && king == false){
            int newX = xPos - 1;
            int newY = yPos + 1;
            if(canPlay(newX,newY)){
                moves += 1;
            }
        }

        //King For White and Black, as they would move the same
        if (king == true){

            //Diagonal Up Left
            if(canPlay(xPos - 1, yPos - 1)){
                moves++;
            }

            //Diagonal Up Right
            if(canPlay(xPos - 1, yPos + 1)){
                moves++;
            }

            //Diagonal Down Left
            if (canPlay(xPos + 1, yPos - 1)){
                moves++;
            }

            //Diagonal Down Right
            if(canPlay(xPos + 1, yPos + 1)){
                moves++;
            }
        }
        return moves;
    }




}