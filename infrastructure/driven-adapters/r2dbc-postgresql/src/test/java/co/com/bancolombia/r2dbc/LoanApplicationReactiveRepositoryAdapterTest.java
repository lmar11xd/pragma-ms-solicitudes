package co.com.bancolombia.r2dbc;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.r2dbc.entity.LoanApplicationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationReactiveRepositoryAdapterTest {

    private LoanApplicationReactiveRepositoryAdapter adapter;
    @Mock
    private LoanApplicationReactiveRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private R2dbcEntityTemplate template;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new LoanApplicationReactiveRepositoryAdapter(repository, objectMapper, template);
    }

    private LoanApplicationEntity buildEntity() {
        return new LoanApplicationEntity(
                "1",
                "123",
                BigDecimal.valueOf(1000),
                12,
                "PERSONAL",
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(90),
                "comment",
                Instant.now(),
                LoanStatus.PENDING.name()
        );
    }

    private LoanApplication buildDomain() {
        return new LoanApplication(
                "1",
                "123",
                BigDecimal.valueOf(1000),
                12,
                "PERSONAL",
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(90),
                "comment",
                Instant.now(),
                LoanStatus.PENDING
        );
    }

    @Test
    void saveShouldReturnDomain() {
        LoanApplication domain = buildDomain();
        LoanApplicationEntity entity = buildEntity();

        when(repository.save(any(LoanApplicationEntity.class)))
                .thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(domain))
                .assertNext(result -> {
                    assertThat(result.getDocumentNumber()).isEqualTo("123");
                    assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
                })
                .verifyComplete();

        verify(repository).save(any(LoanApplicationEntity.class));
    }

    @Test
    void saveShouldMapToDomainExceptionWhenConstraintViolation() {
        LoanApplication domain = buildDomain();

        when(repository.save(any(LoanApplicationEntity.class)))
                .thenReturn(Mono.error(new DataIntegrityViolationException("duplicate key")));

        StepVerifier.create(adapter.save(domain))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(DomainException.class);
                    DomainException dex = (DomainException) ex;
                    assertThat(dex.getErrorCode()).isEqualTo(ErrorCode.DATABASE_CONSTRAINT_VIOLATION);
                })
                .verify();
    }

    @Test
    void findForFiltersShouldReturnDomainObjects() {
        LoanApplicationEntity entity = buildEntity();
        when(template.select(any(Query.class), eq(LoanApplicationEntity.class)))
                .thenReturn(Flux.just(entity));

        StepVerifier.create(adapter.findForFilters(
                        Set.of(LoanStatus.PENDING),
                        "PERSONAL",
                        "123",
                        0,
                        10))
                .assertNext(result -> {
                    assertThat(result.getDocumentNumber()).isEqualTo("123");
                    assertThat(result.getLoanTypeCode()).isEqualTo("PERSONAL");
                })
                .verifyComplete();

        verify(template).select(any(Query.class), eq(LoanApplicationEntity.class));
    }

    @Test
    void countForFiltersShouldReturnCount() {
        when(template.count(any(Query.class), eq(LoanApplicationEntity.class)))
                .thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.countForFilters(
                        Set.of(LoanStatus.PENDING, LoanStatus.REJECTED),
                        null,
                        "123"))
                .expectNext(3L)
                .verifyComplete();

        verify(template).count(any(Query.class), eq(LoanApplicationEntity.class));
    }

    @Test
    void sumApprovedMonthlyDebtByDocumentShouldReturnValue() {
        when(repository.sumApprovedMonthlyDebtByDocument("123"))
                .thenReturn(Mono.just(BigDecimal.valueOf(250)));

        StepVerifier.create(adapter.sumApprovedMonthlyDebtByDocument("123"))
                .expectNext(BigDecimal.valueOf(250))
                .verifyComplete();

        verify(repository).sumApprovedMonthlyDebtByDocument("123");
    }

    @Test
    void sumApprovedMonthlyDebtByDocumentShouldMapLockTimeoutError() {
        when(repository.sumApprovedMonthlyDebtByDocument("123"))
                .thenReturn(Mono.error(new CannotAcquireLockException("timeout")));

        StepVerifier.create(adapter.sumApprovedMonthlyDebtByDocument("123"))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(DomainException.class);
                    assertThat(((DomainException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.DATABASE_LOCK_TIMEOUT);
                })
                .verify();
    }
}
