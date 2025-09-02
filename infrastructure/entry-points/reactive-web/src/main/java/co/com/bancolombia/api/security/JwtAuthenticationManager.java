package co.com.bancolombia.api.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        if (!jwtProvider.validateToken(authToken)) {
            return Mono.empty();
        }

        Claims claims = jwtProvider.getClaims(authToken);
        String email = claims.getSubject();
        List<String> roles = jwtProvider.getRoles(authToken);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return Mono.just(
                new UsernamePasswordAuthenticationToken(
                        email,
                        authToken,
                        authorities
                )
        );
    }
}
