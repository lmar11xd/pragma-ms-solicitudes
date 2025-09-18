package co.com.bancolombia.usecase.changeloanapplicationstatus;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChangeLoanApplicationStatusUseCaseTest {

    private LoanApplicationRepository loanApplicationRepository;
    private ApplicantPort applicantPort;
    private NotificationPort notificationPort;
    private ChangeLoanApplicationStatusUseCase useCase;

    private final LoanApplication loanApplication = new LoanApplication(
            "loan-123",
            "123456789",
            BigDecimal.valueOf(10000),
            12,
            "PERSONAL",
            BigDecimal.valueOf(0.15),
            BigDecimal.valueOf(900),
            "comment",
            Instant.now(),
            Instant.now(),
            LoanStatus.PENDING
    );

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        applicantPort = mock(ApplicantPort.class);
        notificationPort = mock(NotificationPort.class);

        useCase = new ChangeLoanApplicationStatusUseCase(
                loanApplicationRepository, applicantPort, notificationPort
        );
    }

    @Test
    void shouldApproveLoanAndSendEvent() {
        Applicant applicant = Applicant.builder().id("app-123").email("test@mail.com").documentNumber("123456789").build();

        when(loanApplicationRepository.findById("loan-123")).thenReturn(Mono.just(loanApplication));
        when(loanApplicationRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(applicantPort.findApplicantByDocumentNumber("123456789"))
                .thenReturn(Mono.just(applicant));
        when(notificationPort.send(any(Notification.class), any())).thenReturn(Mono.just("OK"));

        StepVerifier.create(useCase.changeStatus("loan-123", LoanStatus.APPROVED))
                .expectNextMatches(result -> result.getStatus() == LoanStatus.APPROVED)
                .verifyComplete();

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationPort).send(notificationCaptor.capture(), eq(EventType.NOTIFICATION_LAMBDA));

        Notification sent = notificationCaptor.getValue();
        assertThat(sent.email()).isEqualTo("test@mail.com");
    }

    @Test
    void shouldRejectLoanAndSendNotification() {
        Applicant applicant = Applicant.builder().id("app-123").email("test@mail.com").documentNumber("123456789").build();

        when(loanApplicationRepository.findById("loan-123")).thenReturn(Mono.just(loanApplication));
        when(loanApplicationRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(applicantPort.findApplicantByDocumentNumber("123456789"))
                .thenReturn(Mono.just(applicant));
        when(notificationPort.send(any(Notification.class), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.changeStatus("loan-123", LoanStatus.REJECTED))
                .expectNextMatches(result -> result.getStatus() == LoanStatus.REJECTED)
                .verifyComplete();
    }

    @Test
    void shouldFailForInvalidStatus() {
        StepVerifier.create(useCase.changeStatus("loan-123", LoanStatus.PENDING))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DomainException.class);
                    assertThat(((DomainException) error).getErrorCode()).isEqualTo(ErrorCode.INVALID_STATUS);
                })
                .verify();
    }

    @Test
    void shouldFailWhenLoanNotFound() {
        when(loanApplicationRepository.findById("loan-123")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.changeStatus("loan-123", LoanStatus.APPROVED))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DomainException.class);
                    assertThat(((DomainException) error).getErrorCode()).isEqualTo(ErrorCode.LOAN_NOT_FOUND);
                })
                .verify();
    }

    @Test
    void shouldFailWhenApplicantNotFound() {
        when(loanApplicationRepository.findById("loan-123")).thenReturn(Mono.just(loanApplication));
        when(loanApplicationRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(applicantPort.findApplicantByDocumentNumber("123456789")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.changeStatus("loan-123", LoanStatus.APPROVED))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(DomainException.class);
                    assertThat(((DomainException) error).getErrorCode()).isEqualTo(ErrorCode.APPLICANT_NOT_FOUND);
                })
                .verify();
    }
}