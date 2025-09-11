package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @Schema(description = "Estado de la solicitud", example = "APPROVED")
        @NotNull LoanStatusDto status
) {
    public enum LoanStatusDto { PENDING, REJECTED, MANUAL_REVIEW, APPROVED}
}
