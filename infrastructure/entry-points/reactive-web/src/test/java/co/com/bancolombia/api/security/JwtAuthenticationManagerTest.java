package co.com.bancolombia.api.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationManagerTest {

    @Mock
    private JwtProvider jwtProvider;

    private JwtAuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationManager = new JwtAuthenticationManager(jwtProvider);
    }

    @Test
    void authenticate_ShouldReturnAuthentication_WhenTokenIsValid() {
        // Arrange
        String token = "valid.jwt.token";
        String email = "user@test.com";
        List<String> roles = List.of("USER", "ADMIN");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(email);

        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(jwtProvider.getClaims(token)).thenReturn(claims);
        when(jwtProvider.getRoles(token)).thenReturn(roles);

        Authentication inputAuth =
                new UsernamePasswordAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(inputAuth);

        // Assert
        StepVerifier.create(result)
                .assertNext(auth -> {
                    assertEquals(email, auth.getPrincipal());
                    assertEquals(token, auth.getCredentials());

                    List<SimpleGrantedAuthority> authorities =
                            (List<SimpleGrantedAuthority>) auth.getAuthorities();

                    assertEquals(2, authorities.size());
                    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
                    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
                })
                .verifyComplete();

        verify(jwtProvider).validateToken(token);
        verify(jwtProvider).getClaims(token);
        verify(jwtProvider).getRoles(token);
    }

    @Test
    void authenticate_ShouldReturnEmpty_WhenTokenIsInvalid() {
        // Arrange
        String token = "invalid.jwt.token";
        when(jwtProvider.validateToken(token)).thenReturn(false);

        Authentication inputAuth =
                new UsernamePasswordAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(inputAuth);

        // Assert
        StepVerifier.create(result)
                .verifyComplete(); // Mono vacío

        verify(jwtProvider).validateToken(token);
        verify(jwtProvider, never()).getClaims(anyString());
        verify(jwtProvider, never()).getRoles(anyString());
    }
}