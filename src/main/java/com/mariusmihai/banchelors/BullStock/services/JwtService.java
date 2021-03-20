package com.mariusmihai.banchelors.BullStock.services;

import com.mariusmihai.banchelors.BullStock.models.User;
import com.mariusmihai.banchelors.BullStock.repositories.TokensRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtService {
    private final String SECRET_KEY = "secretsecretsecretsecretsecretsecretsecretsecret";
    private final Long ACCESS_TOKEN_EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(7);
    private final Long REFRESH_TOKEN_EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(14);

    @Autowired
    private TokensRepository tokensRepository;

    @Transactional
    public Boolean isTokenExpired(String token) {
        if (extractClaim(token, Claims::getExpiration).before(new Date())) {
            this.tokensRepository.deleteByAccessToken(token);
        }
        var tokenFromDb = this.tokensRepository.findByAccessToken(token);
        return tokenFromDb.isEmpty();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token)
                .getBody());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(
                claims,
                user.getEmail(),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(ACCESS_TOKEN_EXPIRATION_MILLIS)
        );
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(
                claims,
                user.getEmail(),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(REFRESH_TOKEN_EXPIRATION_MILLIS)
        );
    }

    private String createToken(Map<String, Object> claims, String subject, Long expireTime) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(expireTime))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, User user) {
        final String email = extractClaim(token, Claims::getSubject);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }
}
