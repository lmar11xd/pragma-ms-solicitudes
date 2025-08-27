package co.com.bancolombia.usecase.loantype;

import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanTypeUseCase {

    private final LoanTypeRepository loanTypeRepository;

    public Mono<Boolean> existsByCode(String code) {
        return loanTypeRepository.existsByCode(code);
    }
}
