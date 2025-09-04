package co.com.bancolombia.api.security;

import co.com.bancolombia.model.security.SecurityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class SecurityAdapterTest {

    private SecurityPort securityAdapter;

    @BeforeEach
    void setUp() {
        securityAdapter = new SecurityAdapter();
    }

    private Mono<String> withAuthContext(Mono<String> action, Authentication auth) {
        return action.contextWrite(
                ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth)))
        );
    }

    @Test
    void getCurrentUserToken_ShouldReturnToken_WhenPresent() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "jwt-token");

        // Act
        Mono<String> result = withAuthContext(securityAdapter.getCurrentUserToken(), auth);

        // Assert
        StepVerifier.create(result)
                .expectNext("jwt-token")
                .verifyComplete();
    }

    @Test
    void getCurrentUserToken_ShouldReturnEmpty_WhenNoCredentials() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", null);

        // Act
        Mono<String> result = withAuthContext(securityAdapter.getCurrentUserToken(), auth);

        // Assert
        StepVerifier.create(result)
                .verifyComplete(); // Mono.empty()
    }

    @Test
    void getAuthenticatedEmail_ShouldReturnPrincipal() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "jwt-token");

        // Act
        Mono<String> result = withAuthContext(securityAdapter.getAuthenticatedEmail(), auth);

        // Assert
        StepVerifier.create(result)
                .expectNext("user@test.com")
                .verifyComplete();
    }

    @Test
    void getAuthenticatedRole_ShouldReturnFirstAuthority() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.com",
                "jwt-token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Act
        Mono<String> result = withAuthContext(securityAdapter.getAuthenticatedRole(), auth);

        // Assert
        StepVerifier.create(result)
                .expectNext("ROLE_ADMIN")
                .verifyComplete();
    }

    @Test
    void getAuthenticatedRole_ShouldReturnEmpty_WhenNoAuthorities() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "jwt-token");

        // Act
        Mono<String> result = withAuthContext(securityAdapter.getAuthenticatedRole(), auth);

        // Assert
        StepVerifier.create(result)
                .verifyComplete(); // Mono.empty()
    }
}
