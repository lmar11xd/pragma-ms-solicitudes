package co.com.bancolombia.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanApplicationDto(
        String id,
        String documentNumber,
        BigDecimal amount,
        Integer termMonths,
        String loanTypeCode,
        BigDecimal insterestRate,
        BigDecimal monthlyInstallment,
        String comment,
        Instant createdAt,
        String status
) {
}
