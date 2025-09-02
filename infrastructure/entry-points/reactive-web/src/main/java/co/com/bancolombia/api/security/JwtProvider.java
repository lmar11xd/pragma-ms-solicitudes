package co.com.bancolombia.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {
    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.expiration}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    /**
     * Genera un token JWT
     */
    public String generateToken(String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email) // Email del usuario
                .claim("roles", roles) // Roles
                .setIssuedAt(Date.from(now)) // Fecha de creación
                .setExpiration(Date.from(now.plusSeconds(expiration))) // Expiración
                .signWith(key, SignatureAlgorithm.HS256) // Firma
                .compact();
    }

    /**
     * Valida un token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("Token expirado");
        } catch (UnsupportedJwtException ex) {
            log.debug("Token no soportado");
        } catch (MalformedJwtException ex) {
            log.debug("Token mal formado");
        } catch (IllegalArgumentException ex) {
            log.debug("Token vacío o inválido");
        }
        return false;
    }

    /**
     * Extrae los Claims del token
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae el username
     */
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public List<String> getRoles(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class); // 👈 recupera lista
    }
}
