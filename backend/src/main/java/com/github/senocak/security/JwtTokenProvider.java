package com.github.senocak.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenProvider {
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private String jwtExpirationInMs;

    private Key signKey;

    @PostConstruct
    public void init() {
        this.signKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Generating the jwt token
     *
     * @param email -- email
     */
    public String generateJwtToken(String email, List<String> roles) {
        return generateToken(email, roles, Long.parseLong(jwtExpirationInMs));
    }

    /**
     * Generating the token
     *
     * @param subject -- userId
     */
    private String generateToken(String subject, List<String> roles, long expirationInMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationInMs))
                .signWith(signKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get the jws claims
     *
     * @param token -- jwt token
     * @return -- expiration date
     */
    private Jws<Claims> getJwsClaims(String token) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
        return Jwts.parserBuilder().setSigningKey(signKey).build().parseClaimsJws(token);
    }

    /**
     * @param token -- jwt token
     * @return -- userName from jwt
     */
    public String getUserEmailFromJWT(String token) {
        return getJwsClaims(token).getBody().getSubject();
    }
}
