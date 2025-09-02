package co.com.bancolombia.api.security;

import co.com.bancolombia.model.security.SecurityPort;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SecurityAdapter implements SecurityPort {
    @Override
    public Mono<String> getCurrentUserToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.getCredentials() instanceof String) // el token se guarda como credencial
                .map(auth -> (String) auth.getCredentials());
    }

    @Override
    public Mono<String> getAuthenticatedEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(AbstractAuthenticationToken.class)
                .map(auth -> (String) auth.getPrincipal()); // asumiendo que el principal = documentNumber
    }

    @Override
    public Mono<String> getAuthenticatedRole() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(AbstractAuthenticationToken.class)
                .flatMap(auth -> {
                    if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                        return Mono.just(auth.getAuthorities().iterator().next().getAuthority());
                    }
                    return Mono.empty();
                });
    }
}
