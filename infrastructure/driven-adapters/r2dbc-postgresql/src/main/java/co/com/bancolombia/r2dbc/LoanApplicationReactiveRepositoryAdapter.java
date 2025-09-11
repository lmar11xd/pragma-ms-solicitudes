package co.com.bancolombia.r2dbc;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.r2dbc.entity.LoanApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.log4j.Log4j2;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Repository
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanApplication,
        LoanApplicationEntity,
        String,
        LoanApplicationReactiveRepository
        > implements LoanApplicationRepository {

    private final R2dbcEntityTemplate template;

    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper, R2dbcEntityTemplate template) {
        super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
        this.template = template;
    }

    @Override
    public Mono<LoanApplication> findById(String id) {
        log.info("Iniciando búsqueda de solicitud de credito con id {}", id);

        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        log.info("Iniciando registro de solicitud de credito con documento {}", loanApplication.getDocumentNumber());

        LoanApplicationEntity entity = toEntity(loanApplication);

        return repository.save(entity)
                .map(this::toDomain)
                .onErrorMap(this::mapToDomainException);
    }

    @Override
    public Flux<LoanApplication> findForFilters(Collection<LoanStatus> statuses, String loanTypeCode, String documentNumber, int offset, int limit) {
        log.info("findForFilters statuses={}, loanType={}, doc={}", statuses, loanTypeCode, documentNumber);

        Pageable pageable = PageRequest.of(offset, limit);

        // Construcción dinámica de criterios
        Criteria criteria = Criteria.where("status").in(statuses);

        if (Objects.nonNull(loanTypeCode)) {
            criteria = criteria.and("loan_type_code").is(loanTypeCode);
        }

        if (Objects.nonNull(documentNumber)) {
            criteria = criteria.and("document_number").is(documentNumber);
        }

        // Construcción de la query con paginación y orden
        Query query = Query.query(criteria)
                .sort(Sort.by(Sort.Order.desc("created_at")))
                .with(pageable);

        return template.select(query, LoanApplicationEntity.class).map(this::toDomain);
    }

    @Override
    public Mono<Long> countForFilters(Collection<LoanStatus> statuses, String loanTypeCode, String documentNumber) {
        log.info("countForFilters statuses={}, loanType={}, doc={}", statuses, loanTypeCode, documentNumber);

        Criteria criteria = Criteria.where("status").in(statuses);

        if (Objects.nonNull(loanTypeCode)) {
            criteria = criteria.and("loan_type_code").is(loanTypeCode);
        }

        if (Objects.nonNull(documentNumber)) {
            criteria = criteria.and("document_number").is(documentNumber);
        }

        return template.count(Query.query(criteria), LoanApplicationEntity.class);
    }

    @Override
    public Mono<BigDecimal> sumApprovedMonthlyDebtByDocument(String documentNumber) {
        log.info("sumApprovedMonthlyDebtByDocumentdoc={}", documentNumber);
        return repository.sumApprovedMonthlyDebtByDocument(documentNumber)
                .onErrorMap(this::mapToDomainException);
    }

    private LoanApplication toDomain(LoanApplicationEntity entity) {
        return new LoanApplication(
                entity.id(),
                entity.documentNumber(),
                entity.amount(),
                entity.termMonths(),
                entity.loanTypeCode(),
                entity.interestRate(),
                entity.monthlyInstallment(),
                entity.comment(),
                entity.createdAt(),
                entity.updatedAt(),
                LoanStatus.valueOf(entity.status())
        );
    }

    private LoanApplicationEntity toEntity(LoanApplication domain) {
        return new LoanApplicationEntity(
                domain.getId(),
                domain.getDocumentNumber(),
                domain.getAmount(),
                domain.getTermMonths(),
                domain.getLoanTypeCode(),
                domain.getInterestRate(),
                domain.getMonthlyInstallment(),
                domain.getComment(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getStatus().name()
        );
    }

    private DomainException mapToDomainException(Throwable throwable) {
        Map<String, Object> details = Map.of("cause", throwable.getMessage());
        if (throwable instanceof DataIntegrityViolationException) {
            return new DomainException(ErrorCode.DATABASE_CONSTRAINT_VIOLATION, details);
        }
        if (throwable instanceof CannotAcquireLockException) {
            return new DomainException(ErrorCode.DATABASE_LOCK_TIMEOUT, details);
        }
        return new DomainException(ErrorCode.DATABASE_ERROR, details);
    }

}
