package co.com.bancolombia.model.events;

import co.com.bancolombia.model.loanapplication.LoanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChangeStatusEvent {
    private String email;
    private LoanStatus status;
    private String documentNumber;
    private BigDecimal amount;
    private Integer termMonths;
    private Instant occurredAt;
}