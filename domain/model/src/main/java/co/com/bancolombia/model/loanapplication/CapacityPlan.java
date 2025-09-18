package co.com.bancolombia.model.loanapplication;

import co.com.bancolombia.model.events.AmortizationEntry;

import java.math.BigDecimal;
import java.util.List;

public record CapacityPlan(
        String status,
        BigDecimal maxDebtCapacity,
        BigDecimal currentMonthlyDebt,
        BigDecimal availableDebtCapacity,
        BigDecimal loanInstallment,
        List<AmortizationEntry> schedule
) {
}
