package co.com.bancolombia.api.service;

import co.com.bancolombia.api.dto.CapacityRequest;
import co.com.bancolombia.api.dto.CreateLoanApplicationRequest;
import co.com.bancolombia.model.events.AmortizationEntry;
import co.com.bancolombia.model.loanapplication.*;
import co.com.bancolombia.usecase.calculatecapacity.CalculateCapacityUseCase;
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
    private CalculateCapacityUseCase calculateCapacityUseCase;

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
        CreateLoanApplicationRequest request = new CreateLoanApplicationRequest(
                "99999",
                new BigDecimal("1000.0"),
                12,
                "HOME",
                new BigDecimal("5.0"),
                ""
        );

        LoanApplication loan = LoanApplication.builder()
                .id("123")
                .documentNumber("99999")
                .amount(new BigDecimal("1000.0"))
                .termMonths(12)
                .loanTypeCode("HOME")
                .interestRate(new BigDecimal("5.0"))
                .monthlyInstallment(new BigDecimal("85.61"))
                .comment("")
                .createdAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build();

        when(loanApplicationUseCase.create(any())).thenReturn(Mono.just(loan));

        StepVerifier.create(service.create(request))
                .expectNextMatches(dto -> dto.id().equals("123") && dto.documentNumber().equals("99999"))
                .verifyComplete();

        verify(loanApplicationUseCase, times(1)).create(any(LoanApplication.class));
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

    @Test
    void shouldCalculateCapacity() {
        CapacityRequest request = new CapacityRequest(
                "99999",
                new BigDecimal("3000.0"),
                12,
                new BigDecimal("12.0"),
                new BigDecimal("500.0")
        );

        CapacityPlan expectedPlan = new CapacityPlan(
                "APPROVED",
                new BigDecimal("3000.0"),
                new BigDecimal("500.0"),
                new BigDecimal("2500.0"),
                new BigDecimal("500.0"),
                List.of(
                        new AmortizationEntry(1, new BigDecimal("85.61"), new BigDecimal("12.50"), new BigDecimal("73.11"), new BigDecimal("2926.89")),
                        new AmortizationEntry(2, new BigDecimal("85.61"), new BigDecimal("10.28"), new BigDecimal("75.33"), new BigDecimal("2851.56"))
                )
        );

        when(calculateCapacityUseCase.calculate(any())).thenReturn(Mono.just(expectedPlan));

        StepVerifier.create(service.calculateCapacityDebt(request))
                .expectNextMatches(capacity -> capacity.loanInstallment().equals(new BigDecimal("500.0")))
                .verifyComplete();

        verify(calculateCapacityUseCase, times(1)).calculate(any());
    }
}