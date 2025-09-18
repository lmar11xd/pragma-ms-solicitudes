package co.com.bancolombia.usecase.changeloanapplicationstatus;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.notification.EventType;
import co.com.bancolombia.model.notification.Notification;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class ChangeLoanApplicationStatusUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicantPort applicantPort;
    private final NotificationPort notificationPort;

    public Mono<LoanApplication> changeStatus(String id, LoanStatus newStatus) {
        if (newStatus != LoanStatus.APPROVED && newStatus != LoanStatus.REJECTED) {
            return Mono.error(new DomainException(ErrorCode.INVALID_STATUS));
        }

        return loanApplicationRepository.findById(id)
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.LOAN_NOT_FOUND)))
                .flatMap(loanApplication -> {
                    LoanApplication loanUpdated = new LoanApplication(
                            loanApplication.getId(),
                            loanApplication.getDocumentNumber(),
                            loanApplication.getAmount(),
                            loanApplication.getTermMonths(),
                            loanApplication.getLoanTypeCode(),
                            loanApplication.getInterestRate(),
                            loanApplication.getMonthlyInstallment(),
                            loanApplication.getComment(),
                            loanApplication.getCreatedAt(),
                            Instant.now(),
                            newStatus
                    );

                    return loanApplicationRepository.save(loanUpdated)
                            .flatMap(loanSaved -> applicantPort
                                    .findApplicantByDocumentNumber(loanSaved.getDocumentNumber())
                                    .switchIfEmpty(Mono.error(new DomainException(ErrorCode.APPLICANT_NOT_FOUND)))
                                    .flatMap(applicant -> sendNotification(loanSaved, applicant.getEmail()).thenReturn(loanSaved))
                            );
                });
    }

    private Mono<Void> sendNotification(LoanApplication loanApplication, String email) {
        String subject = loanApplication.getStatus() == LoanStatus.APPROVED
                ? "Solicitud de crédito Aprobada"
                : "Solicitud de crédito Rechazada";

        String fullBody = buildBody(loanApplication);

        Notification notification = new Notification(
                email,
                subject,
                fullBody
        );

        return notificationPort.send(notification, EventType.NOTIFICATION_LAMBDA).then();
    }

    private static String buildBody(LoanApplication loanApplication) {
        String body = loanApplication.getStatus() == LoanStatus.APPROVED
                ? "Felicidades, tu solicitud de crédito ha sido aprobada."
                : "Lamentamos informarte que tu solicitud de crédito ha sido rechazada.";

        return body +
                "\n" +
                "Monto: " + loanApplication.getAmount() + "\n" +
                "Plazo (meses): " + loanApplication.getTermMonths() + "\n" +
                "Tipo de Crédito: " + loanApplication.getLoanTypeCode() + "\n" +
                "Tasa de Interés: " + loanApplication.getInterestRate() + "\n" +
                "Cuota Mensual: " + loanApplication.getMonthlyInstallment() + "\n";
    }
}
