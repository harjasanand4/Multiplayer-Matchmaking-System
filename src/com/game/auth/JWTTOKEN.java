package com.game.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JWTTOKEN {
    // Static, persistent secret key (at least 32 bytes for HS256)
    private static final String SECRET_KEY_STRING = "thisisaverylongsecretkeyforjwt123456";
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(
            SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    private static final long EXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000; // 30 days

    /**
     * Creates a token given a username with a 30-day expiration.
     * @param username The username to embed in the token.
     * @return Token as a String.
     * @author: Tan Michael Olsen
     */
    public static String generateToken(String username) {
        Instant now = Instant.now();
        Instant expirationTime = now.plus(30, ChronoUnit.DAYS);
        Date expirationDate = Date.from(expirationTime);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Checks if a token is valid.
     * @param token Token to validate.
     * @return True if valid, false if invalid (expired, tampered, etc.).
     * @author: Tan Michael Olsen
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            System.out.println("Valid Token exist");
            return true;
        } catch (JwtException e) {
            System.out.println("Valid Token does not exist: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the username from the token.
     * @param token Token to parse.
     * @return The username embedded in the token.
     * @author: Tan Michael Olsen
     */
    public static String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}