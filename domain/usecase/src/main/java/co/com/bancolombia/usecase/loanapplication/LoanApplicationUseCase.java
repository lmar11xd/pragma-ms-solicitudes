package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicantPort applicantPort;

    public Mono<LoanApplication> create(LoanApplication loanApplication) {

        if (loanApplication.getDocumentNumber().isBlank())
            return Mono.error(new DomainException(ErrorCode.REQUERID_DOCUMENTNUMBER));

        if (loanApplication.getAmount() == null || loanApplication.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            return Mono.error(new DomainException(ErrorCode.INVALID_AMOUNT));

        if (loanApplication.getTermMonths() == null || loanApplication.getTermMonths() <= 0)
            return Mono.error(new DomainException(ErrorCode.INVALID_TERMMONTHS));

        loanApplication.setStatus(LoanStatus.PENDING); // Por defecto pendiente de revision

        return loanTypeRepository
                .existsByCode(loanApplication.getLoanTypeCode())
                .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return applicantPort
                                        .findApplicantByDocumentNumber(loanApplication.getDocumentNumber())
                                        .switchIfEmpty(Mono.error(new DomainException(ErrorCode.APPLICANT_NOT_FOUND)))
                                        .flatMap(applicant -> loanApplicationRepository.save(loanApplication));
                            }

                            return Mono.error(new DomainException(ErrorCode.INVALID_LOANTYPE));
                        }
                );
    }
}
