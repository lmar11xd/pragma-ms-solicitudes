package co.com.bancolombia.model.loanapplication.gateways;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Collection;

public interface LoanApplicationRepository {
    Mono<LoanApplication> findById(String id);

    Mono<LoanApplication> save(LoanApplication loanApplication);

    Flux<LoanApplication> findForFilters(
            Collection<LoanStatus> statuses,
            @Nullable String loanTypeCode,
            @Nullable String documentNumber,
            int offset,
            int limit
    );

    Mono<Long> countForFilters(
            Collection<LoanStatus> statuses,
            @Nullable String loanTypeCode,
            @Nullable String documentNumber
    );

    // Suma de la cuota mensual de solicitudes aprobadas del solicitante
    Mono<BigDecimal> sumApprovedMonthlyDebtByDocument(String documentNumber);
}
