package co.com.bancolombia.r2dbc;

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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void saveShouldReturnLoanApplicationWhenRepositorySavesSuccessfully() {
        // given
        LoanApplication domain = LoanApplication.builder()
                .id("123")
                .documentNumber("987654321")
                .amount(new java.math.BigDecimal("5000.00"))
                .termMonths(12)
                .loanTypeCode("PERSONAL")
                .interestRate(new BigDecimal(10))
                .comment("Testing loan")
                .createdAt(java.time.Instant.now())
                .status(LoanStatus.PENDING)
                .build();

        LoanApplicationEntity entity = new LoanApplicationEntity(
                domain.getId(),
                domain.getDocumentNumber(),
                domain.getAmount(),
                domain.getTermMonths(),
                domain.getLoanTypeCode(),
                domain.getInterestRate(),
                domain.getMonthlyInstallment(),
                domain.getComment(),
                domain.getCreatedAt(),
                domain.getStatus().name()
        );

        when(repository.save(any(LoanApplicationEntity.class))).thenReturn(Mono.just(entity));

        // when - then
        StepVerifier.create(adapter.save(domain))
                .expectNextMatches(result ->
                        result.getId().equals(domain.getId()) &&
                                result.getDocumentNumber().equals(domain.getDocumentNumber()) &&
                                result.getAmount().equals(domain.getAmount()) &&
                                result.getTermMonths().equals(domain.getTermMonths()) &&
                                result.getLoanTypeCode().equals(domain.getLoanTypeCode()) &&
                                result.getComment().equals(domain.getComment()) &&
                                result.getStatus() == domain.getStatus()
                )
                .verifyComplete();

        verify(repository, times(1)).save(any(LoanApplicationEntity.class));
    }

    @Test
    void saveShouldPropagateErrorWhenRepositoryFails() {
        // given
        LoanApplication domain = LoanApplication.builder()
                .id("456")
                .documentNumber("123456789")
                .amount(new java.math.BigDecimal("10000.00"))
                .termMonths(24)
                .loanTypeCode("MORTGAGE")
                .comment("Testing error")
                .createdAt(java.time.Instant.now())
                .status(LoanStatus.REJECTED)
                .build();

        when(repository.save(any(LoanApplicationEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("DB failure")));

        // when - then
        StepVerifier.create(adapter.save(domain))
                .expectErrorMatches(err -> err instanceof RuntimeException &&
                        err.getMessage().equals("DB failure"))
                .verify();

        verify(repository, times(1)).save(any(LoanApplicationEntity.class));
    }
}
