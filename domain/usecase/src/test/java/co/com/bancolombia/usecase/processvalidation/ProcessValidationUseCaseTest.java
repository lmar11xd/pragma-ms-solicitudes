package co.com.bancolombia.usecase.processvalidation;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.events.AmortizationEntry;
import co.com.bancolombia.model.events.ValidationResult;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.notification.EventType;
import co.com.bancolombia.model.notification.Notification;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProcessValidationUseCaseTest {

    private LoanApplicationRepository loanApplicationRepository;
    private NotificationPort notificationPort;
    private ProcessValidationUseCase useCase;

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        notificationPort = mock(NotificationPort.class);
        useCase = new ProcessValidationUseCase(loanApplicationRepository, notificationPort);
    }

    @Test
    void shouldUpdateLoanAndSendNotification_WhenLoanExists() {
        // Arrange
        LoanApplication loan = LoanApplication.builder()
                .id("123")
                .status(LoanStatus.PENDING)
                .updatedAt(Instant.now())
                .build();

        ValidationResult result = new ValidationResult(
                "123",
                "0",
                "123",
                "test@email.com",
                LoanStatus.APPROVED,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(150),
                List.of(new AmortizationEntry(1, BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(150), BigDecimal.valueOf(900))),
                Instant.now()
        );

        LoanApplication updatedLoan = loan.toBuilder().status(LoanStatus.APPROVED).build();

        when(loanApplicationRepository.findById("123")).thenReturn(Mono.just(loan));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(Mono.just(updatedLoan));
        when(notificationPort.send(any(Notification.class), eq(EventType.NOTIFICATION_LAMBDA))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(useCase.process(result))
                .expectNextMatches(l -> l.getStatus().equals(LoanStatus.APPROVED))
                .verifyComplete();

        // Assert
        verify(loanApplicationRepository).findById("123");
        verify(loanApplicationRepository).save(any(LoanApplication.class));
        verify(notificationPort).send(any(Notification.class), eq(EventType.NOTIFICATION_LAMBDA));
    }

    @Test
    void shouldReturnError_WhenLoanNotFound() {
        // Arrange
        ValidationResult result = new ValidationResult(
                "not-found",
                "0",
                "123",
                "test@email.com",
                LoanStatus.REJECTED,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(),
                Instant.now()
        );

        when(loanApplicationRepository.findById("not-found")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.process(result))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode().equals(ErrorCode.LOAN_NOT_FOUND))
                .verify();

        verify(notificationPort, never()).send(any(), any());
    }

    @Test
    void shouldBuildNotificationBodyCorrectly() {
        // Arrange
        LoanApplication loan = LoanApplication.builder()
                .id("123")
                .status(LoanStatus.PENDING)
                .updatedAt(Instant.now())
                .build();

        ValidationResult result = new ValidationResult(
                "123",
                "0",
                "123",
                "client@mail.com",
                LoanStatus.APPROVED,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(300),
                List.of(new AmortizationEntry(1, BigDecimal.valueOf(200), BigDecimal.valueOf(100), BigDecimal.valueOf(300), BigDecimal.valueOf(1800))),
                Instant.now()
        );

        LoanApplication updatedLoan = loan.toBuilder().status(LoanStatus.APPROVED).build();

        when(loanApplicationRepository.findById("123")).thenReturn(Mono.just(loan));
        when(loanApplicationRepository.save(any())).thenReturn(Mono.just(updatedLoan));
        when(notificationPort.send(any(), eq(EventType.NOTIFICATION_LAMBDA))).thenReturn(Mono.empty());

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);

        // Act
        StepVerifier.create(useCase.process(result))
                .expectNextMatches(loanSaved -> loanSaved.getStatus().equals(LoanStatus.APPROVED))
                .verifyComplete();

        // Assert body
        verify(notificationPort).send(notificationCaptor.capture(), eq(EventType.NOTIFICATION_LAMBDA));
        Notification sent = notificationCaptor.getValue();

        assertThat(sent.email()).isEqualTo("client@mail.com");
        assertThat(sent.subject()).isEqualTo("Resultado de tu solicitud de crédito");
        assertThat(sent.message()).contains("Capacidad Máxima: 2000")
                .contains("Deuda Actual: 500")
                .contains("Capacidad Disponible: 1500")
                .contains("Cuota Nueva: 300")
                .contains("Plan de Pagos:");
    }
}