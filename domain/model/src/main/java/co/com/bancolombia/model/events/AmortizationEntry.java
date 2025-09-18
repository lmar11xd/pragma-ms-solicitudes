package co.com.bancolombia.model.events;

import java.math.BigDecimal;

public record AmortizationEntry(
        int month,
        BigDecimal payment,
        BigDecimal interest,
        BigDecimal principal,
        BigDecimal balance
) {
}
