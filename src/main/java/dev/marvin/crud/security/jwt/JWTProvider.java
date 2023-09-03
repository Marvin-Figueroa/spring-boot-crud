package dev.marvin.crud.security.jwt;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import dev.marvin.crud.security.dto.JwtDTO;
import dev.marvin.crud.security.entity.MainUser;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTProvider {
    private final static Logger logger = LoggerFactory.getLogger(JWTProvider.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.millis}")
    private int expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        MainUser  mainUser = (MainUser) authentication.getPrincipal();
        List<String> roles = mainUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(mainUser.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();


    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody().getSubject();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT");
        }catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT");
        }catch (ExpiredJwtException e) {
            logger.error("Expired JWT");
        }catch (SignatureException e) {
            logger.error("Invalid signature");
        }catch (IllegalArgumentException e) {
            logger.error("JWT is empty");
        }
        return false;
    }

    public String refreshToken(JwtDTO jwtDTO) throws ParseException {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(jwtDTO.getToken());
        } catch (ExpiredJwtException e) {
            JWT jwt = JWTParser.parse(jwtDTO.getToken());
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String username = claims.getSubject();
            List<String> roles = (List<String>) claims.getClaim("roles");

            return Jwts.builder()
                    .setSubject(username)
                    .claim("roles", roles)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getKey(), SignatureAlgorithm.HS512)
                    .compact();

        }

        return null;

    }
}
