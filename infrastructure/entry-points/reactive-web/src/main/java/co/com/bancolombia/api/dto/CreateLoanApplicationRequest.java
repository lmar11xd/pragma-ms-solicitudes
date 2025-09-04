package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateLoanApplicationRequest(
        @Schema(description = "Documento de identidad del solicitante", example = "12345678")
        @NotBlank String documentNumber,
        @Schema(description = "Monto a solicitar", example = "8000")
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Schema(description = "Plazo en meses del credito", example = "12")
        @NotNull @Min(1) Integer termMonths,
        @Schema(description = "Codigo del tipo de credito", example = "100")
        @NotBlank String loanTypeCode,
        @Schema(description = "Tasa de Interés Anual", example = "15")
        @NotNull @DecimalMin("0.01") BigDecimal interestRate,
        @Schema(description = "Comentario", example = "Credito para trabajo")
        String comment
) {
}