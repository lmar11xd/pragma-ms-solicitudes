package co.com.bancolombia.usecase.processvalidation;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.events.ValidationResult;
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
public class ProcessValidationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final NotificationPort notificationPort;

    public Mono<LoanApplication> process(ValidationResult response) {
        return loanApplicationRepository.findById(response.loanApplicationId())
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.LOAN_NOT_FOUND)))
                .flatMap(loan -> {
                    LoanStatus newStatus = response.status();

                    LoanApplication updatedLoan = loan.toBuilder()
                            .status(newStatus)
                            .updatedAt(Instant.now())
                            .build();

                    return loanApplicationRepository
                            .save(updatedLoan)
                            .flatMap(saved -> sendPaymentPlan(response).thenReturn(saved));
                });
    }

    private Mono<Void> sendPaymentPlan(ValidationResult response) {
        StringBuilder body = new StringBuilder();

        body.append("Resultado de la validación automática: ")
                .append(response.status().getDescription()).append("\n")
                .append("Capacidad Máxima: ").append(response.maxDebtCapacity()).append("\n")
                .append("Deuda Actual: ").append(response.currentMonthlyDebt()).append("\n")
                .append("Capacidad Disponible: ").append(response.availableDebt()).append("\n")
                .append("Cuota Nueva: ").append(response.loanInstallment()).append("\n\n")
                .append("Plan de Pagos:\n");

        response.paymentPlan().forEach(cuota -> body
                .append("Cuota ").append(cuota.month())
                .append(": Capital=").append(cuota.principal())
                .append(", Intereses=").append(cuota.interest())
                .append(", Total=").append(cuota.payment())
                .append(", Saldo Restante=").append(cuota.balance())
                .append("\n"));

        Notification notification = new Notification(
                response.applicantEmail(),
                "Resultado de tu solicitud de crédito",
                body.toString()
        );

        return notificationPort.send(notification, EventType.NOTIFICATION_LAMBDA).then();
    }
}
