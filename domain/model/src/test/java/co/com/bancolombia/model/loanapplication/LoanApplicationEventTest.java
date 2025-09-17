package co.com.bancolombia.model.loanapplication;

import co.com.bancolombia.model.events.ChangeStatusEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationEventTest {

    @Test
    void shouldBuildEventWithBuilder() {
        Instant now = Instant.now();

        ChangeStatusEvent event = ChangeStatusEvent.builder()
                .email("test@domain.com")
                .status(LoanStatus.APPROVED)
                .documentNumber("123456789")
                .amount(BigDecimal.valueOf(5000))
                .termMonths(24)
                .occurredAt(now)
                .build();

        assertThat(event.getEmail()).isEqualTo("test@domain.com");
        assertThat(event.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(event.getDocumentNumber()).isEqualTo("123456789");
        assertThat(event.getAmount()).isEqualByComparingTo("5000");
        assertThat(event.getTermMonths()).isEqualTo(24);
        assertThat(event.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void shouldCopyEventWithToBuilder() {
        ChangeStatusEvent original = ChangeStatusEvent.builder()
                .email("user@domain.com")
                .status(LoanStatus.REJECTED)
                .documentNumber("987654321")
                .amount(BigDecimal.TEN)
                .termMonths(12)
                .occurredAt(Instant.EPOCH)
                .build();

        ChangeStatusEvent copy = original.toBuilder()
                .email("new@domain.com")
                .build();

        assertThat(copy.getEmail()).isEqualTo("new@domain.com");
        assertThat(copy.getStatus()).isEqualTo(LoanStatus.REJECTED);
        assertThat(copy.getDocumentNumber()).isEqualTo("987654321");
        assertThat(copy.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(copy.getTermMonths()).isEqualTo(12);
        assertThat(copy.getOccurredAt()).isEqualTo(Instant.EPOCH);
    }

    @Test
    void shouldCreateEventWithAllArgsConstructor() {
        Instant now = Instant.now();
        ChangeStatusEvent event = new ChangeStatusEvent(
                "constructor@domain.com",
                LoanStatus.APPROVED,
                "111222333",
                BigDecimal.ONE,
                6,
                now
        );

        assertThat(event.getEmail()).isEqualTo("constructor@domain.com");
        assertThat(event.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(event.getDocumentNumber()).isEqualTo("111222333");
        assertThat(event.getAmount()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(event.getTermMonths()).isEqualTo(6);
        assertThat(event.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void shouldUseNoArgsConstructorAndSetters() {
        ChangeStatusEvent event = new ChangeStatusEvent();
        Instant now = Instant.now();

        event.setEmail("setter@domain.com");
        event.setStatus(LoanStatus.PENDING);
        event.setDocumentNumber("444555666");
        event.setAmount(BigDecimal.valueOf(2000));
        event.setTermMonths(36);
        event.setOccurredAt(now);

        assertThat(event.getEmail()).isEqualTo("setter@domain.com");
        assertThat(event.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(event.getDocumentNumber()).isEqualTo("444555666");
        assertThat(event.getAmount()).isEqualByComparingTo("2000");
        assertThat(event.getTermMonths()).isEqualTo(36);
        assertThat(event.getOccurredAt()).isEqualTo(now);
    }
}
