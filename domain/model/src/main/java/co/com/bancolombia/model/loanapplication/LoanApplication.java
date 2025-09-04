package co.com.bancolombia.model.loanapplication;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {
    private String id;
    private String documentNumber;
    private BigDecimal amount;
    private Integer termMonths;
    private String loanTypeCode;
    private BigDecimal interestRate;
    private BigDecimal monthlyInstallment;
    private String comment;
    private Instant createdAt;
    private LoanStatus status;
}
