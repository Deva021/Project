package com.minintercom.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private static String secret = "your-default-secret-key-change-this-in-production";
    private static Algorithm algorithm = Algorithm.HMAC256(secret);

    @Test
    public void testValidateValidToken() {
        String tenantId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withClaim("tenant_id", tenantId)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
                .sign(algorithm);

        DecodedJWT decodedJWT = JwtService.validateToken(token);
        assertNotNull(decodedJWT);
        assertEquals(tenantId, JwtService.getClaim(decodedJWT, "tenant_id"));
    }

    @Test
    public void testValidateExpiredToken() {
        String token = JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() - 3600000))
                .sign(algorithm);

        DecodedJWT decodedJWT = JwtService.validateToken(token);
        assertNull(decodedJWT);
    }

    @Test
    public void testValidateInvalidSignature() {
        String token = JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
                .sign(Algorithm.HMAC256("wrong-secret"));

        DecodedJWT decodedJWT = JwtService.validateToken(token);
        assertNull(decodedJWT);
    }

    @Test
    public void testGetClaimFromNull() {
        assertNull(JwtService.getClaim(null, "any"));
    }
}
