package co.com.bancolombia.api.service;

import co.com.bancolombia.api.dto.AdvisorReviewResponse;
import co.com.bancolombia.api.dto.LoanApplicationDto;
import co.com.bancolombia.api.mapper.LoanApplicationMapper;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.usecase.changeloanapplicationstatus.ChangeLoanApplicationStatusUseCase;
import co.com.bancolombia.usecase.loanapplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

@Log4j2
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationUseCase loanApplicationUseCase;

    private final ChangeLoanApplicationStatusUseCase changeLoanApplicationStatusUseCase;

    private final TransactionalOperator transactionalOperator;

    public Mono<LoanApplicationDto> create(LoanApplication loanApplication) {
        return transactionalOperator.transactional(
                loanApplicationUseCase.create(loanApplication)
        ).map(saved -> {
            log.info("Solicitud de credito creada con id {}", saved.getId());
            return LoanApplicationMapper.toDto(saved);
        });
    }

    public Mono<LoanApplication> changeStatus(String id, String status) {
        return transactionalOperator.transactional(
                changeLoanApplicationStatusUseCase.changeStatus(id, LoanStatus.valueOf(status))
        );
    }

    public Mono<AdvisorReviewResponse> listApplications(
            int page, int size,
            @Nullable String statuses,
            @Nullable String loanTypeCode,
            @Nullable String documentNumber
    ) {
        return loanApplicationUseCase.listApplications(page, size, statuses, loanTypeCode, documentNumber)
                .map(pr -> AdvisorReviewResponse.builder()
                        .content(pr.content())
                        .totalElements(pr.totalElements())
                        .page(pr.page())
                        .size(pr.size())
                        .build()
                );
    }
}
