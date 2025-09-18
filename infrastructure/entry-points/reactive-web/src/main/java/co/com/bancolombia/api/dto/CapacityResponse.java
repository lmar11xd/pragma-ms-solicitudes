package co.com.bancolombia.api.dto;

import co.com.bancolombia.model.events.AmortizationEntry;

import java.math.BigDecimal;
import java.util.List;

public record CapacityResponse(
        String status,
        BigDecimal maxDebtCapacity,
        BigDecimal currentMonthlyDebt,
        BigDecimal availableDebtCapacity,
        BigDecimal loanInstallment,
        List<AmortizationEntry> schedule
) {
}
