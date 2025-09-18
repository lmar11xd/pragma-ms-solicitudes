package co.com.bancolombia.model.events;

import co.com.bancolombia.model.loanapplication.LoanStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ValidationResult(
        String loanApplicationId,       // ID de la solicitud validada
        String applicantId,             // ID del solicitante
        String documentNumber,          // Número de documento del solicitante
        String applicantEmail,          // Email del solicitante
        LoanStatus status,              // Resultado de la validación (APROBADO, RECHAZADO, REVISION_MANUAL)
        BigDecimal maxDebtCapacity,     // Capacidad máxima de endeudamiento
        BigDecimal currentMonthlyDebt,  // Deuda mensual actual
        BigDecimal availableDebt,       // Capacidad disponible luego de restar deuda
        BigDecimal loanInstallment,     // Cuota del nuevo préstamo
        List<AmortizationEntry> paymentPlan,   // Plan de pagos (detalle capital e interés)
        Instant occurredAt
) {
}
