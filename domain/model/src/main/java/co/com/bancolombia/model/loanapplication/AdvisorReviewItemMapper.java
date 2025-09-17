package co.com.bancolombia.model.loanapplication;

import co.com.bancolombia.model.applicant.Applicant;

import java.math.BigDecimal;

public class AdvisorReviewItemMapper {

    public static AdvisorReviewItem from(LoanApplication loan, Applicant applicant, BigDecimal debtApprovedApplications) {
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
    }
}
