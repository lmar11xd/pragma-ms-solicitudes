package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.AdvisorReviewItem;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.Page;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.security.SecurityPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicantPort applicantPort;
    private final SecurityPort securityPort;

    public Mono<LoanApplication> create(LoanApplication loanApplication) {

        if (loanApplication.getDocumentNumber().isBlank())
            return Mono.error(new DomainException(ErrorCode.REQUERID_DOCUMENTNUMBER));

        if (loanApplication.getAmount() == null || loanApplication.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            return Mono.error(new DomainException(ErrorCode.INVALID_AMOUNT));

        if (loanApplication.getTermMonths() == null || loanApplication.getTermMonths() <= 0)
            return Mono.error(new DomainException(ErrorCode.INVALID_TERMMONTHS));

        loanApplication.setStatus(LoanStatus.PENDING); // Por defecto pendiente de revision

        // Calcular cuota mensual
        loanApplication.setMonthlyInstallment(
                calculateMonthlyInstallment(
                        loanApplication.getAmount(),
                        loanApplication.getInterestRate(),
                        loanApplication.getTermMonths()
                )
        );

        return loanTypeRepository
                .existsByCode(loanApplication.getLoanTypeCode())
                .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return securityPort.getAuthenticatedEmail()
                                        .switchIfEmpty(Mono.error(new DomainException(ErrorCode.UNAUTHORIZED)))
                                        .flatMap(emailFromToken -> applicantPort
                                                .findApplicantByDocumentNumber(loanApplication.getDocumentNumber()) // Obtiene solicitante del MS Autenticacion
                                                .switchIfEmpty(Mono.error(new DomainException(ErrorCode.APPLICANT_NOT_FOUND)))
                                                .flatMap(applicant -> {
                                                            if (!applicant.getEmail().equals(emailFromToken)) { // Email no coincide
                                                                return Mono.error(new DomainException(ErrorCode.UNAUTHORIZED_ACTION));
                                                            }

                                                            return loanApplicationRepository.save(loanApplication);
                                                        }
                                                )

                                        );
                            }

                            return Mono.error(new DomainException(ErrorCode.INVALID_LOANTYPE));
                        }
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
                                        .defaultIfEmpty(BigDecimal.ZERO)
                        )
                        .map(tuple -> {
                            Applicant applicant = tuple.getT1();
                            BigDecimal debtApprovedApplications = tuple.getT2();

                            return AdvisorReviewItem.builder()
                                    .amount(loan.getAmount())
                                    .termMonths(loan.getTermMonths())
                                    .email(applicant.getEmail())
                                    .names(applicant.getNames() + " " + applicant.getLastNames())
                                    .documentNumber(applicant.getDocumentNumber())
                                    .loanType(loan.getLoanTypeCode())
                                    .interestRate(loan.getInterestRate())
                                    .statusApplication(loan.getStatus().name())
                                    .baseSalary(applicant.getBaseSalary())
                                    .totalMonthlyDebtApprovedRequest(debtApprovedApplications)
                                    .build();
                        }));

        return rows.collectList()
                .zipWith(total)
                .map(tuple -> Page.<AdvisorReviewItem>builder()
                        .content(tuple.getT1())
                        .totalElements(tuple.getT2())
                        .page(page)
                        .size(size)
                        .build());
    }

    /* Para calcular la cuota mensual (monthlyInstallment),
    * lo usual es aplicar la fórmula de anualidades del sistema francés (pagos fijos).

    Fórmula de la cuota mensual
        Cuota = (M*i)/(1-(1+i)^-n)

    Donde:
        M = Monto del préstamo (amount)
        i = Tasa de interés mensual (interestRate / 12)
        n = Plazo en meses (termMonths)
    * */
    private BigDecimal calculateMonthlyInstallment(BigDecimal amount, BigDecimal annualRate, int termMonths) {
        if (amount == null || annualRate == null || termMonths <= 0) {
            throw new IllegalArgumentException("Invalid loan parameters");
        }

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64) // 12% -> 0.12
                .divide(BigDecimal.valueOf(12), MathContext.DECIMAL64); // -> 0.01

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // Caso especial: tasa 0%
            return amount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        // Fórmula de anualidad: (M * i) / (1 - (1 + i)^-n)
        BigDecimal numerator = amount.multiply(monthlyRate);
        BigDecimal denominator = BigDecimal.ONE.subtract(
                BigDecimal.ONE.add(monthlyRate).pow(-termMonths, MathContext.DECIMAL64)
        );

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
