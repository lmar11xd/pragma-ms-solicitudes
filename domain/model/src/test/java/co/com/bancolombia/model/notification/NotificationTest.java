package co.com.bancolombia.model.notification;

import co.com.bancolombia.model.loanapplication.LoanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void shouldBuildNotificationWithBuilder() {
        Instant now = Instant.now();

        Notification notification = Notification.builder()
                .email("test@domain.com")
                .status(LoanStatus.APPROVED)
                .documentNumber("123456789")
                .amount(BigDecimal.valueOf(5000))
                .termMonths(24)
                .occurredAt(now)
                .build();

        assertThat(notification.getEmail()).isEqualTo("test@domain.com");
        assertThat(notification.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(notification.getDocumentNumber()).isEqualTo("123456789");
        assertThat(notification.getAmount()).isEqualByComparingTo("5000");
        assertThat(notification.getTermMonths()).isEqualTo(24);
        assertThat(notification.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void shouldCopyNotificationWithToBuilder() {
        Notification original = Notification.builder()
                .email("user@domain.com")
                .status(LoanStatus.REJECTED)
                .documentNumber("987654321")
                .amount(BigDecimal.TEN)
                .termMonths(12)
                .occurredAt(Instant.EPOCH)
                .build();

        Notification copy = original.toBuilder()
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
    void shouldCreateNotificationWithAllArgsConstructor() {
        Instant now = Instant.now();
        Notification notification = new Notification(
                "constructor@domain.com",
                LoanStatus.APPROVED,
                "111222333",
                BigDecimal.ONE,
                6,
                now
        );

        assertThat(notification.getEmail()).isEqualTo("constructor@domain.com");
        assertThat(notification.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(notification.getDocumentNumber()).isEqualTo("111222333");
        assertThat(notification.getAmount()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(notification.getTermMonths()).isEqualTo(6);
        assertThat(notification.getOccurredAt()).isEqualTo(now);
    }

    @Test
    void shouldUseNoArgsConstructorAndSetters() {
        Notification notification = new Notification();
        Instant now = Instant.now();

        notification.setEmail("setter@domain.com");
        notification.setStatus(LoanStatus.PENDING);
        notification.setDocumentNumber("444555666");
        notification.setAmount(BigDecimal.valueOf(2000));
        notification.setTermMonths(36);
        notification.setOccurredAt(now);

        assertThat(notification.getEmail()).isEqualTo("setter@domain.com");
        assertThat(notification.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(notification.getDocumentNumber()).isEqualTo("444555666");
        assertThat(notification.getAmount()).isEqualByComparingTo("2000");
        assertThat(notification.getTermMonths()).isEqualTo(36);
        assertThat(notification.getOccurredAt()).isEqualTo(now);
    }
}
