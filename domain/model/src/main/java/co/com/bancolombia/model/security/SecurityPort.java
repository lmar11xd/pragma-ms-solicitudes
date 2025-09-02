package co.com.bancolombia.model.security;

import reactor.core.publisher.Mono;

public interface SecurityPort {
    Mono<String> getCurrentUserToken();
    Mono<String> getAuthenticatedEmail();
    Mono<String> getAuthenticatedRole();
}