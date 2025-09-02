package co.com.bancolombia.api.config;

import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.api.security.JwtAuthenticationManager;
import co.com.bancolombia.api.security.JwtProvider;
import co.com.bancolombia.api.security.JwtSecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static co.com.bancolombia.api.util.Utils.toJson;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Swagger
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // Registrar solicitudes de credito → solo CUSTOMER
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitudes")
                        .hasAnyRole("CUSTOMER")

                        // Todos los demás endpoints requieren autenticación
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authenticationManager(jwtAuthenticationManager())
                .securityContextRepository(securityContextRepository())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .build();
    }

    @Bean
    public JwtAuthenticationManager jwtAuthenticationManager() {
        return new JwtAuthenticationManager(jwtProvider);
    }

    @Bean
    public JwtSecurityContextRepository securityContextRepository() {
        return new JwtSecurityContextRepository(jwtAuthenticationManager());
    }

    @Bean
    public ServerAuthenticationEntryPoint unauthorizedEntryPoint() {
        return (exchange, ex) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "No autenticado: se requiere un token válido.",
                    exchange.getRequest().getPath().value(),
                    Map.of()
            );

            byte[] bytes = toJson(error).getBytes(StandardCharsets.UTF_8);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        };
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);

            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    "Acceso denegado: no tiene permisos para esta operación.",
                    exchange.getRequest().getPath().value(),
                    Map.of()
            );

            byte[] bytes = toJson(error).getBytes(StandardCharsets.UTF_8);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        };
    }
}
