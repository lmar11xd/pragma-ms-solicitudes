package co.com.bancolombia.api.dto;

import java.math.BigDecimal;

public record CapacityRequest(
        String documentNumber,
        BigDecimal amount,
        Integer termMonths,
        BigDecimal annualInterestRate,
        BigDecimal applicantBaseSalary
) {
}
