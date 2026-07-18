package com.game.auth;

/**
 * THIS IS JUST A TRIAL FILE TO TEST MY CODE
 * @author
 */
public class trial {
    public static void main(String[] args) {

        // Example username
        String username = "testnjk";

        // Generate the JWT token using the JwtUtil class
        String token = JWTTOKEN.generateToken(username);

        // Print the token to the console
        System.out.println("Generated Token: " + token);
        System.out.println("Username: "+ JWTTOKEN.getUsernameFromToken(token));
        //Thread.sleep(1000);
        System.out.println(JWTTOKEN.validateToken(token));
    }
}
