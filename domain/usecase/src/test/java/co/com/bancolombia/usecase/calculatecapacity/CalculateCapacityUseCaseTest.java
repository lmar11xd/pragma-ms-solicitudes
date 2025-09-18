package co.com.bancolombia.usecase.calculatecapacity;

import co.com.bancolombia.model.loanapplication.CapacityDebt;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CalculateCapacityUseCaseTest {

    private LoanApplicationRepository repository;
    private CalculateCapacityUseCase useCase;

    @BeforeEach
    void init() {
        repository = Mockito.mock(LoanApplicationRepository.class);
        useCase = new CalculateCapacityUseCase(repository);
    }

    @Test
    void shouldApproveLoanWhenInstallmentIsWithinCapacity() {
        // given
        CapacityDebt request = new CapacityDebt(
                "99999",
                new BigDecimal("2000.00"), // salario
                new BigDecimal("3000.00"), // monto prestamo
                new BigDecimal("5"),    // interes anual
                12
        );

        when(repository.sumApprovedMonthlyDebtByDocument("99999"))
                .thenReturn(Mono.just(BigDecimal.ZERO));

        // when & then
        StepVerifier.create(useCase.calculate(request))
                .assertNext(plan -> {
                    // 35% de 2000 = 700 -> capacidad máxima
                    // deuda actual = 0
                    // debe aprobar
                    assertEquals(LoanStatus.APPROVED.name(), plan.status());
                    assertEquals(0, plan.maxDebtCapacity().compareTo(new BigDecimal("700.00")));
                    assertEquals(BigDecimal.ZERO, plan.currentMonthlyDebt());
                })
                .verifyComplete();

        verify(repository, times(1)).sumApprovedMonthlyDebtByDocument("99999");
    }

    @Test
    void shouldRejectLoanWhenInstallmentExceedsCapacity() {
        // given
        CapacityDebt request = new CapacityDebt(
                "88888",
                new BigDecimal("1000.00"), // salario
                new BigDecimal("10000.00"), // monto muy alto
                new BigDecimal("20"),     // interes anual alto
                6
        );

        when(repository.sumApprovedMonthlyDebtByDocument("88888"))
                .thenReturn(Mono.just(BigDecimal.ZERO));

        StepVerifier.create(useCase.calculate(request))
                .assertNext(plan -> {
                    assert plan.status().equals(LoanStatus.REJECTED.name());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnManualReviewWhenAmountExceeds5TimesSalary() {
        // given
        CapacityDebt request = new CapacityDebt(
                "77777",
                new BigDecimal("1000.00"),  // salario
                new BigDecimal("6000.00"),  // mayor a 5 * salario
                new BigDecimal("1"),     // interes bajo
                48
        );

        when(repository.sumApprovedMonthlyDebtByDocument("77777"))
                .thenReturn(Mono.just(BigDecimal.ZERO));

        StepVerifier.create(useCase.calculate(request))
                .assertNext(plan -> assertEquals(LoanStatus.MANUAL_REVIEW.name(), plan.status()))
                .verifyComplete();
    }

    @Test
    void shouldHandleEmptyRepositoryResponse() {
        // given
        CapacityDebt request = new CapacityDebt(
                "00000",
                new BigDecimal("1500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("5"),
                12
        );

        when(repository.sumApprovedMonthlyDebtByDocument("00000"))
                .thenReturn(Mono.empty()); // repo devuelve vacío

        StepVerifier.create(useCase.calculate(request))
                .assertNext(plan -> {
                    assert plan.currentMonthlyDebt().compareTo(BigDecimal.ZERO) == 0;
                })
                .verifyComplete();
    }
}