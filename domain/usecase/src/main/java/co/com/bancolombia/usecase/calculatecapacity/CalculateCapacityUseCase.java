package co.com.bancolombia.usecase.calculatecapacity;

import co.com.bancolombia.model.events.AmortizationEntry;
import co.com.bancolombia.model.loanapplication.CapacityDebt;
import co.com.bancolombia.model.loanapplication.CapacityPlan;
import co.com.bancolombia.model.loanapplication.LoanCalculator;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class CalculateCapacityUseCase {
    private final LoanApplicationRepository loanRepository;

    public Mono<CapacityPlan> calculate(CapacityDebt capacityDebt) {
        // 1) capacidad maxima
        BigDecimal maxDebtCapacity = capacityDebt.applicantBaseSalary().multiply(new BigDecimal("0.35"));

        // 2) deuda mensual actual
        return loanRepository.sumApprovedMonthlyDebtByDocument(capacityDebt.documentNumber())
                .defaultIfEmpty(BigDecimal.ZERO)
                .flatMap(currentMonthlyDebt -> {
                    BigDecimal newInstallment = LoanCalculator.monthlyInstallment(capacityDebt.amount(), capacityDebt.annualInterestRate(), capacityDebt.termMonths());
                    
                    BigDecimal availableDebt = maxDebtCapacity.subtract(currentMonthlyDebt);
                    
                    LoanStatus loanStatus = determineLoanStatus(capacityDebt, newInstallment, availableDebt);

                    List<AmortizationEntry> schedule = LoanCalculator.amortizationSchedule(capacityDebt.amount(), capacityDebt.annualInterestRate(), capacityDebt.termMonths());
                    CapacityPlan resp = new CapacityPlan(loanStatus.name(), maxDebtCapacity, currentMonthlyDebt, availableDebt, newInstallment, schedule);
                    return Mono.just(resp);
                });
    }

    private LoanStatus determineLoanStatus(
            CapacityDebt capacityDebt,
            BigDecimal newInstallment,
            BigDecimal availableDebt
    ) {
        LoanStatus loanStatus = newInstallment.compareTo(availableDebt) <= 0
                ? LoanStatus.APPROVED
                : LoanStatus.REJECTED;

        if (loanStatus == LoanStatus.APPROVED &&
                capacityDebt.amount().compareTo(
                        capacityDebt.applicantBaseSalary().multiply(new BigDecimal(5))
                ) > 0) {
            loanStatus = LoanStatus.MANUAL_REVIEW;
        }
        return loanStatus;
    }
}
