package co.com.bancolombia.model.events;

import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.loanapplication.LoanApplication;

import java.math.BigDecimal;
import java.time.Instant;

public class EventsFactory {

    public static ValidationEvent from(LoanApplication loan, Applicant applicant, BigDecimal monthlyDebt) {
        return ValidationEvent.builder()
                .loanApplicationId(loan.getId())
                .applicantId(applicant.getId())
                .documentNumber(applicant.getDocumentNumber())
                .applicantBaseSalary(applicant.getBaseSalary())
                .amount(loan.getAmount())
                .annualInterestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .loanTypeCode(loan.getLoanTypeCode())
                .currentMonthlyDebt(monthlyDebt)
                .occurredAt(Instant.now())
                .build();
    }

    public static ChangeStatusEvent from(LoanApplication loan, Applicant applicant) {
        return ChangeStatusEvent.builder()
                .email(applicant.getEmail())
                .status(loan.getStatus())
                .documentNumber(applicant.getDocumentNumber())
                .amount(loan.getAmount())
                .termMonths(loan.getTermMonths())
                .occurredAt(Instant.now())
                .build();
    }
}
