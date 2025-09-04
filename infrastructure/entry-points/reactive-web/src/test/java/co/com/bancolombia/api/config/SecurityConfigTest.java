package co.com.bancolombia.api.config;

import co.com.bancolombia.api.security.JwtAuthenticationManager;
import co.com.bancolombia.api.security.JwtProvider;
import co.com.bancolombia.api.security.JwtSecurityContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private JwtProvider jwtProvider;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        jwtProvider = Mockito.mock(JwtProvider.class);
        securityConfig = new SecurityConfig(jwtProvider);
    }

    @Test
    void shouldCreateSecurityWebFilterChain() {
        var http = org.springframework.security.config.web.server.ServerHttpSecurity.http();
        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);

        assertThat(chain).isNotNull();
    }

    @Test
    void shouldCreateJwtAuthenticationManager() {
        JwtAuthenticationManager manager = securityConfig.jwtAuthenticationManager();
        assertThat(manager).isNotNull();
    }

    @Test
    void shouldCreateJwtSecurityContextRepository() {
        JwtSecurityContextRepository repo = securityConfig.securityContextRepository();
        assertThat(repo).isNotNull();
    }

    @Test
    void unauthorizedEntryPointShouldReturn401() {
        var entryPoint = securityConfig.unauthorizedEntryPoint();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").build()
        );

        Mono<Void> result = entryPoint.commence(exchange, new AuthenticationException("Unauthorized") {
        });

        StepVerifier.create(result).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("Unauthorized");
    }

    @Test
    void accessDeniedHandlerShouldReturn403() {
        ServerAccessDeniedHandler handler = securityConfig.accessDeniedHandler();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").build()
        );

        Mono<Void> result = handler.handle(exchange, new AccessDeniedException("Forbidden"));

        StepVerifier.create(result).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("Forbidden");
    }
}
