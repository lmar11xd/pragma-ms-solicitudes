package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.AdvisorReviewItem;
import co.com.bancolombia.model.loanapplication.AdvisorReviewItemMapper;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanApplicationValidator;
import co.com.bancolombia.model.loanapplication.LoanCalculator;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.Page;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.notification.EventType;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import co.com.bancolombia.model.security.SecurityPort;
import co.com.bancolombia.model.events.EventsFactory;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicantPort applicantPort;
    private final SecurityPort securityPort;
    private final NotificationPort notificationPort;

    public Mono<LoanApplication> create(LoanApplication loanApplication) {
        // Validaciones
        try {
            LoanApplicationValidator.validateBasicData(loanApplication);
        } catch (DomainException ex) {
            return Mono.error(ex);
        }

        // Inicializar campos
        loanApplication.setStatus(LoanStatus.PENDING);
        loanApplication.setMonthlyInstallment(
                LoanCalculator.calculateMonthlyInstallment(
                        loanApplication.getAmount(),
                        loanApplication.getInterestRate(),
                        loanApplication.getTermMonths()
                )
        );

        return loanTypeRepository
                .findByCode(loanApplication.getLoanTypeCode())
                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.INVALID_LOANTYPE)))
                .flatMap(loanType ->
                        securityPort.getAuthenticatedEmail()
                                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.UNAUTHORIZED)))
                                .flatMap(emailFromToken -> applicantPort
                                        .findApplicantByDocumentNumber(loanApplication.getDocumentNumber()) // Obtiene solicitante del MS Autenticacion
                                        .switchIfEmpty(Mono.error(new DomainException(ErrorCode.APPLICANT_NOT_FOUND)))
                                        .flatMap(applicant -> {
                                                    if (!applicant.getEmail().equals(emailFromToken)) { // Email no coincide
                                                        return Mono.error(new DomainException(ErrorCode.UNAUTHORIZED_ACTION));
                                                    }

                                                    return loanApplicationRepository.save(loanApplication)
                                                            .flatMap(savedLoan -> {
                                                                if (Boolean.TRUE.equals(loanType.getValidationAutomatic())) {
                                                                    return loanApplicationRepository
                                                                            .sumApprovedMonthlyDebtByDocument(applicant.getDocumentNumber())
                                                                            .defaultIfEmpty(BigDecimal.ZERO)
                                                                            .flatMap(monthlyDebt -> {
                                                                                var event = EventsFactory.from(savedLoan, applicant, monthlyDebt);
                                                                                return notificationPort
                                                                                        .send(event, EventType.CAPACITY_LAMBDA)
                                                                                        .thenReturn(savedLoan);
                                                                            });
                                                                }

                                                                return Mono.just(savedLoan);
                                                            });
                                                }
                                        )
                                )
                );
    }

    public Mono<Page<AdvisorReviewItem>> listApplications(
            int page, int size,
            @Nullable String statuses,
            @Nullable String loanTypeCode,
            @Nullable String documentNumber
    ) {
        // Estados sujetos a revisión del asesor
        var lststatuses = List.of(LoanStatus.PENDING, LoanStatus.REJECTED, LoanStatus.MANUAL_REVIEW); // Por defecto

        if (statuses != null) {
            lststatuses = Arrays.stream(statuses.split(",")).map(LoanStatus::valueOf).toList();
        }

        int offset = page * size;

        Mono<Long> total = loanApplicationRepository.countForFilters(lststatuses, loanTypeCode, documentNumber);

        Flux<AdvisorReviewItem> rows = loanApplicationRepository
                .findForFilters(lststatuses, loanTypeCode, documentNumber, offset, size)
                .flatMap(loan -> Mono.zip(
                                applicantPort.findApplicantByDocumentNumber(loan.getDocumentNumber()),
                                loanApplicationRepository
                                        .sumApprovedMonthlyDebtByDocument(loan.getDocumentNumber())
                                        .defaultIfEmpty(BigDecimal.ZERO))
                        .map(tuple ->
                                AdvisorReviewItemMapper.from(loan, tuple.getT1(), tuple.getT2())
                        )
                );

        return rows.collectList()
                .zipWith(total)
                .map(tuple -> Page.<AdvisorReviewItem>builder()
                        .content(tuple.getT1())
                        .totalElements(tuple.getT2())
                        .page(page)
                        .size(size)
                        .build());
    }
}
