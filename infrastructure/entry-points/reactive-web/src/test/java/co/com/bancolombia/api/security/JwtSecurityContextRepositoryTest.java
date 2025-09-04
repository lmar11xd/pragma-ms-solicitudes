package co.com.bancolombia.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class JwtSecurityContextRepositoryTest {

    @Mock
    private JwtAuthenticationManager authenticationManager;

    private ServerSecurityContextRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new JwtSecurityContextRepository(authenticationManager);
    }

    @Test
    void load_ShouldReturnSecurityContext_WhenBearerTokenValid() {
        // Arrange
        String token = "valid.jwt.token";
        Authentication auth =
                new UsernamePasswordAuthenticationToken("user@test.com", token);

        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(auth));

        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<SecurityContext> result = repository.load(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(context -> {
                    Authentication authentication = context.getAuthentication();
                    assertEquals("user@test.com", authentication.getPrincipal());
                    assertEquals(token, authentication.getCredentials());
                })
                .verifyComplete();

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void load_ShouldReturnEmpty_WhenNoAuthorizationHeader() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<SecurityContext> result = repository.load(exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete(); // Mono.empty()
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void load_ShouldReturnEmpty_WhenHeaderDoesNotStartWithBearer() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Basic abc123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<SecurityContext> result = repository.load(exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void load_ShouldReturnEmpty_WhenAuthenticationFails() {
        // Arrange
        String token = "invalid.jwt.token";
        when(authenticationManager.authenticate(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<SecurityContext> result = repository.load(exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void save_ShouldReturnEmpty() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = repository.save(exchange, null);

        // Assert
        StepVerifier.create(result).verifyComplete();
    }
}