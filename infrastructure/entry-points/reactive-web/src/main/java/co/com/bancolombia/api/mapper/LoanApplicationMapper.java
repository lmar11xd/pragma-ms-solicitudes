package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.CreateLoanApplicationRequest;
import co.com.bancolombia.api.dto.LoanApplicationDto;
import co.com.bancolombia.model.loanapplication.LoanApplication;

import java.time.Instant;

public final class LoanApplicationMapper {

    public static LoanApplication toDomain(CreateLoanApplicationRequest dto) {
        return new LoanApplication(
                null,
                dto.documentNumber(),
                dto.amount(),
                dto.termMonths(),
                dto.loanTypeCode(),
                dto.comment(),
                Instant.now(),
                null
        );
    }

    public static LoanApplicationDto toDto(LoanApplication domain) {
        return new LoanApplicationDto(
                domain.getId(),
                domain.getDocumentNumber(),
                domain.getAmount(),
                domain.getTermMonths(),
                domain.getLoanTypeCode(),
                domain.getComment(),
                domain.getCreatedAt(),
                domain.getStatus().name()
        );
    }

    private LoanApplicationMapper() {
    }
}
