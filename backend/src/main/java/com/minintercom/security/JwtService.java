package com.minintercom.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Handles JWT validation and claim extraction using the java-jwt library.
 */
public class JwtService {
    private static String secret;
    private static Algorithm algorithm;
    private static JWTVerifier verifier;

    static {
        try (InputStream input = JwtService.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
                // Fallback for development if needed, but should be in properties
                secret = "your-default-secret-key-change-this-in-production";
            } else {
                prop.load(input);
                secret = prop.getProperty("jwt.secret");
                if (secret == null || secret.isEmpty()) {
                    secret = "your-default-secret-key-change-this-in-production";
                }
            }
            algorithm = Algorithm.HMAC256(secret);
            verifier = JWT.require(algorithm).build();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Validates a JWT token and returns the decoded object if valid.
     * 
     * @param token The JWT token to validate.
     * @return The decoded JWT if valid, null otherwise.
     */
    public static DecodedJWT validateToken(String token) {
        try {
            return verifier.verify(token);
        } catch (Exception e) {
            System.err.println("JWT Validation Failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts a specific claim from a decoded JWT.
     * 
     * @param decodedJWT The decoded JWT.
     * @param claimName  The name of the claim to extract.
     * @return The claim value as a string, or null if not found.
     */
    public static String getClaim(DecodedJWT decodedJWT, String claimName) {
        if (decodedJWT == null)
            return null;
        return decodedJWT.getClaim(claimName).asString();
    }
}
