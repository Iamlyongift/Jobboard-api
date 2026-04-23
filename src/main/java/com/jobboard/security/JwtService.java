package com.jobboard.security;

import com.jobboard.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {


    @Value("${jwt.secret}")
    private String secretKey;          // Base64-encoded 256-bit secret

    @Value("${jwt.expiration}")
    private long expirationMs;         // e.g. 86400000 = 24 hours in ms

    public String generateToken(User user) {

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role",   user.getRole());    // e.g. "ROLE_ADMIN"
        extraClaims.put("userId", user.getId());       // e.g. 42

        return buildToken(extraClaims, user.getEmail());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(extraClaims)                        // custom claims first
                .setSubject(subject)                           // sub = email
                .setIssuedAt(new Date(now))                    // iat = now
                .setExpiration(new Date(now + expirationMs))   // exp = now + 24 h
                .signWith(getSigningKey())
                .compact();                                    // → "xxxxx.yyyyy.zzzzz"
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();     // returns payload claims
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        String emailInToken = extractEmail(token);
        return emailInToken.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
