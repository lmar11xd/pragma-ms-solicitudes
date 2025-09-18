package co.com.bancolombia.model.loanapplication;

import java.math.BigDecimal;

public record CapacityDebt(
        String documentNumber,
        BigDecimal applicantBaseSalary,
        BigDecimal amount,
        BigDecimal annualInterestRate,
        Integer termMonths
) {
}
