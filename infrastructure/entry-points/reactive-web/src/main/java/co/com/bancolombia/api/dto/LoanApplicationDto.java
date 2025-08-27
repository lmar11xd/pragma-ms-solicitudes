package co.com.bancolombia.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanApplicationDto(
        String id,
        String documentNumber,
        BigDecimal amount,
        Integer termMonths,
        String loanTypeCode,
        String comment,
        Instant createdAt,
        String status
) {
}
