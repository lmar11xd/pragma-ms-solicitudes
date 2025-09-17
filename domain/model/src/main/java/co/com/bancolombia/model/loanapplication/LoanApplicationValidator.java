package co.com.bancolombia.model.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;

import java.math.BigDecimal;

public class LoanApplicationValidator {

    public static void validateBasicData(LoanApplication loanApplication) {
        if (loanApplication.getDocumentNumber() == null || loanApplication.getDocumentNumber().isBlank()) {
            throw new DomainException(ErrorCode.REQUERID_DOCUMENTNUMBER);
        }

        if (loanApplication.getAmount() == null || loanApplication.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(ErrorCode.INVALID_AMOUNT);
        }

        if (loanApplication.getTermMonths() == null || loanApplication.getTermMonths() <= 0) {
            throw new DomainException(ErrorCode.INVALID_TERMMONTHS);
        }
    }
}
