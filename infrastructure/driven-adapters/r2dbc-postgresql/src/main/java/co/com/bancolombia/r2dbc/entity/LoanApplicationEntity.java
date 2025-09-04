package co.com.bancolombia.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("loan_applications")
public record LoanApplicationEntity(
        @Id String id,
        @Column("document_number")
        String documentNumber,
        BigDecimal amount,
        @Column("term_months")
        Integer termMonths,
        @Column("loan_type_code")
        String loanTypeCode,
        @Column("interest_rate")
        BigDecimal interestRate,
        @Column("monthly_installment")
        BigDecimal monthlyInstallment,
        String comment,
        @Column("created_at")
        Instant createdAt,
        String status
) {
}