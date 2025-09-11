package co.com.bancolombia.model.notification;

import co.com.bancolombia.model.loanapplication.LoanStatus;
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
public class Notification {
    private String email;
    private LoanStatus status;
    private String documentNumber;
    private BigDecimal amount;
    private Integer termMonths;
    private Instant occurredAt;
}
