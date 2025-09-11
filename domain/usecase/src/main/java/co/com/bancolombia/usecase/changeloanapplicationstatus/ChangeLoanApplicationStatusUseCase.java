package co.com.bancolombia.usecase.changeloanapplicationstatus;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.notification.Notification;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import co.com.bancolombia.model.security.SecurityPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class ChangeLoanApplicationStatusUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final SecurityPort securityPort;
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
                            .flatMap(loanSaved -> securityPort
                                    .getCurrentUserToken()
                                    .switchIfEmpty(Mono.error(new DomainException(ErrorCode.UNAUTHORIZED)))
                                    .flatMap(token -> applicantPort
                                            .findApplicantByDocumentNumber(loanSaved.getDocumentNumber(), token)
                                    )
                                    .switchIfEmpty(Mono.error(new DomainException(ErrorCode.APPLICANT_NOT_FOUND)))
                                    .flatMap(applicant -> {
                                        Notification notification = Notification
                                                .builder()
                                                .email(applicant.getEmail())
                                                .status(loanSaved.getStatus())
                                                .documentNumber(applicant.getDocumentNumber())
                                                .amount(loanSaved.getAmount())
                                                .termMonths(loanSaved.getTermMonths())
                                                .occurredAt(Instant.now())
                                                .build();

                                        return notificationPort.send(notification).thenReturn(loanSaved);
                                    })
                            );
                });
    }
}
