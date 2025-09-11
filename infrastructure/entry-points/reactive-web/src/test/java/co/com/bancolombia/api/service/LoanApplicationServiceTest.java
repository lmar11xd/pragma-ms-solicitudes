package co.com.bancolombia.api.service;

import co.com.bancolombia.model.loanapplication.AdvisorReviewItem;
import co.com.bancolombia.model.loanapplication.Page;
import co.com.bancolombia.usecase.changeloanapplicationstatus.ChangeLoanApplicationStatusUseCase;
import co.com.bancolombia.usecase.loanapplication.LoanApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import co.com.bancolombia.api.dto.AdvisorReviewResponse;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoanApplicationServiceTest {
    @Mock
    private LoanApplicationUseCase loanApplicationUseCase;

    @Mock
    private ChangeLoanApplicationStatusUseCase changeLoanApplicationStatusUseCase;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private LoanApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(transactionalOperator.transactional(Mockito.<Mono<Object>>any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldCreateLoanApplication() {
        LoanApplication loan = LoanApplication.builder()
                .id("123")
                .documentNumber("99999")
                .amount(new BigDecimal("1000.0"))
                .createdAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build();

        when(loanApplicationUseCase.create(any())).thenReturn(Mono.just(loan));

        StepVerifier.create(service.create(loan))
                .expectNextMatches(dto -> dto.id().equals("123") && dto.documentNumber().equals("99999"))
                .verifyComplete();

        verify(loanApplicationUseCase, times(1)).create(loan);
    }

    @Test
    void shouldListApplications() {
        // Simula PageResult
        var pageResult = new Page<>(
                List.of(
                        AdvisorReviewItem
                                .builder()
                                .amount(new BigDecimal(1000))
                                .termMonths(12)
                                .documentNumber("111")
                                .email("test@example.com")
                                .statusApplication("PENDING")
                                .build()
                ),
                1, 0, 10
        );

        when(loanApplicationUseCase.listApplications(0, 10, null, null, null))
                .thenReturn(Mono.<Page<AdvisorReviewItem>>just(pageResult));

        StepVerifier.create(service.listApplications(0, 10, null, null, null))
                .expectNextMatches(resp -> resp instanceof AdvisorReviewResponse
                        && resp.totalElements() == 1
                        && resp.content().size() == 1)
                .verifyComplete();

        verify(loanApplicationUseCase, times(1))
                .listApplications(0, 10, null, null, null);
    }

    @Test
    void shouldChangeStatus() {
        LoanApplication updated = LoanApplication.builder()
                .id("123")
                .status(LoanStatus.APPROVED)
                .build();

        when(changeLoanApplicationStatusUseCase.changeStatus(eq("123"), eq(LoanStatus.APPROVED)))
                .thenReturn(Mono.just(updated));

        StepVerifier.create(service.changeStatus("123", "APPROVED"))
                .expectNextMatches(l -> l.getStatus() == LoanStatus.APPROVED)
                .verifyComplete();

        verify(changeLoanApplicationStatusUseCase, times(1))
                .changeStatus("123", LoanStatus.APPROVED);
    }
}