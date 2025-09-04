package co.com.bancolombia.model.loanapplication;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AdvisorReviewItem(
        BigDecimal amount,
        Integer termMonths,
        String email,
        String names,
        String loanType,
        BigDecimal interestRate,
        String statusApplication,
        BigDecimal baseSalary,
        BigDecimal totalMonthlyDebtApprovedRequest
) {}
