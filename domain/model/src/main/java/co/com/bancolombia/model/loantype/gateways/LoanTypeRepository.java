package co.com.bancolombia.model.loantype.gateways;

import co.com.bancolombia.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> findByCode(String code);
    Mono<Boolean> existsByCode(String code);
    Mono<Boolean> isValidationAutomatic(String code);
}
