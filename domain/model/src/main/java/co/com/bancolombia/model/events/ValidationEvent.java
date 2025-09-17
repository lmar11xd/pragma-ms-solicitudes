package co.com.bancolombia.model.events;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ValidationEvent {
    private String loanApplicationId;
    private String applicantId;
    private String documentNumber;
    private BigDecimal applicantBaseSalary;
    private BigDecimal amount;
    private BigDecimal annualInterestRate;
    private Integer termMonths;
    private String loanTypeCode;
    private BigDecimal currentMonthlyDebt;
    private Instant occurredAt;
}
