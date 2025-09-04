package co.com.bancolombia.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();

        // Generamos clave secreta de 256 bits
        SecretKey secretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secretBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        // Inyectamos los valores privados simulando @Value
        ReflectionTestUtils.setField(jwtProvider, "secret", secretBase64);
        ReflectionTestUtils.setField(jwtProvider, "expiration", 3600L);

        jwtProvider.init(); // inicializa la key
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String email = "user@test.com";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = jwtProvider.generateToken(email, roles);

        assertNotNull(token);

        Claims claims = jwtProvider.getClaims(token);
        assertEquals(email, claims.getSubject());
        assertEquals(roles, claims.get("roles", List.class));
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        String token = jwtProvider.generateToken("valid@test.com", List.of("ROLE_USER"));
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtProvider.validateToken(invalidToken));
    }

    @Test
    void getEmail_ShouldReturnCorrectEmail() {
        String token = jwtProvider.generateToken("email@test.com", List.of("ROLE_USER"));
        String email = jwtProvider.getEmail(token);
        assertEquals("email@test.com", email);
    }

    @Test
    void getRoles_ShouldReturnCorrectRoles() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = jwtProvider.generateToken("roles@test.com", roles);
        List<String> extractedRoles = jwtProvider.getRoles(token);

        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_USER"));
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
    }
}

