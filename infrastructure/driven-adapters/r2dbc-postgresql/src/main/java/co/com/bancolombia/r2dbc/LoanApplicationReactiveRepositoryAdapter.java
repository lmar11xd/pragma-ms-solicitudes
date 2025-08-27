package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.r2dbc.entity.LoanApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanApplication,
        LoanApplicationEntity,
        String,
        LoanApplicationReactiveRepository
        > implements LoanApplicationRepository {
    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        log.info("Iniciando registro de solicitud de credito con documento {}", loanApplication.getDocumentNumber());

        LoanApplicationEntity entity = new LoanApplicationEntity(
                loanApplication.getId(),
                loanApplication.getDocumentNumber(),
                loanApplication.getAmount(),
                loanApplication.getTermMonths(),
                loanApplication.getLoanTypeCode(),
                loanApplication.getComment(),
                loanApplication.getCreatedAt(),
                loanApplication.getStatus().name()
        );

        return repository
                .save(entity)
                .map(e ->
                        new LoanApplication(
                                e.id(),
                                e.documentNumber(),
                                e.amount(),
                                e.termMonths(),
                                e.loanTypeCode(),
                                e.comment(),
                                e.createdAt(),
                                LoanStatus.valueOf(e.status())
                        )
                );
    }

}
