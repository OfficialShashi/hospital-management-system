
package com.hms.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	// Demo Base64 secret. Production mein secure secret use karein.
	private static final String SECRET_KEY = System.getenv("JWT_SECRET");

	private final long expirationTime = 1000 * 60 * 60; // 1 hour

	private Key getSigningKey() {

    if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
        throw new IllegalStateException(
            "JWT_SECRET environment variable is not configured"
        );
    }

    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
	if (keyBytes.length < 32) {
    throw new IllegalStateException(
        "JWT_SECRET must be at least 32 bytes (256 bits)"
    );
	}
    return Keys.hmacShaKeyFor(keyBytes);
	}


	public String generateToken(String email) {

		return Jwts.builder().subject(email).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationTime)).signWith(getSigningKey()).compact();
	}

	public String extractEmail(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, String email) {
		String tokenEmail = extractEmail(token);
		return tokenEmail.equals(email) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {

		return !extractAllClaims(token).getExpiration().after(new Date());

	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith((javax.crypto.SecretKey) getSigningKey()).build().parseSignedClaims(token)
				.getPayload();
	}
}
