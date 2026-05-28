package com.edu.authsystem.service;

import com.edu.authsystem.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class managing JSON Web Token operations.
 * Handles generation of access tokens, cryptographic signature signing, and parsing claims.
 */
@Service
public class JwtService {

    // Secure base64 or plaintext signing key. Must be at least 256-bits (32 bytes).
    @Value("${security.jwt.secret-key:default-secure-and-extremely-long-secret-key-32-bytes}")
    private String secretKeyString;

    // Default expiration is 15 minutes (900,000 milliseconds) for access tokens
    @Value("${security.jwt.expiration-ms:900000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate an Access Token with claims for email, roles, and user ID.
     */
    public String generateToken(String email, Set<Role> roles, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        
        // Map roles enums directly to string lists
        List<String> roleList = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        extraClaims.put("roles", roleList);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract the username (email) claim from the JWT token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract roles claim from the JWT token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Extract a specific claim from a token using a resolver function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validate access token signature integrity and expiration.
     */
    public boolean isTokenValid(String token) {
        try {
            final Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.after(new Date());
        } catch (Exception e) {
            // Catches ExpiredJwtException, SignatureException, MalformedJwtException
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
