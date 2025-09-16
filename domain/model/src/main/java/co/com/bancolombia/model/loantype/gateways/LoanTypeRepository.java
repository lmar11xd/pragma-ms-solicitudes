package co.com.bancolombia.model.loantype.gateways;

import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<Boolean> existsByCode(String code);
    Mono<Boolean> isValidationAutomatic(String code);
}
