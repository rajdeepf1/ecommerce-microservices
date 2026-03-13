package com.ecomm.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    // Read from application.properties (stored in GitHub config repo)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;      // 900000 = 15 minutes

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;     // 604800000 = 7 days

    // =====================================================
    // TOKEN GENERATION
    // =====================================================

    // Generate ACCESS token for a logged-in user
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Store roles inside the token payload
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claims.put("roles", roles);
        claims.put("type", "ACCESS");

        return buildToken(claims, userDetails.getUsername(), accessTokenExpiryMs);
    }

    // Generate REFRESH token — minimal claims, longer expiry
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");

        return buildToken(claims, userDetails.getUsername(), refreshTokenExpiryMs);
    }

    // Core token builder used by both methods above
    private String buildToken(Map<String, Object> claims,
                              String subject,
                              long expiryMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .claims(claims)           // payload data (roles, type)
                .subject(subject)         // email stored as subject
                .issuedAt(now)            // when token was created
                .expiration(expiry)       // when token expires
                .signWith(getSigningKey()) // sign with secret key
                .compact();               // build to string
    }

    // =====================================================
    // TOKEN EXTRACTION
    // =====================================================

    // Get email from token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Get expiry date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Get roles from token
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    // Get token type (ACCESS or REFRESH)
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // Generic claim extractor — pass any function to extract any field
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parse and return all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // use same key to verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =====================================================
    // TOKEN VALIDATION
    // =====================================================

    // Validate access token against the user loaded from DB
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            final String tokenType = extractTokenType(token);

            return email.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && "ACCESS".equals(tokenType);

        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // Validate refresh token (less strict — only checks expiry and type)
    public boolean validateRefreshToken(String token) {
        try {
            extractAllClaims(token); // throws if invalid/expired
            return !isTokenExpired(token)
                    && "REFRESH".equals(extractTokenType(token));
        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    // Check if token is past its expiry date
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =====================================================
    // SIGNING KEY
    // =====================================================

    // Decode Base64 secret and build the HMAC-SHA256 signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}