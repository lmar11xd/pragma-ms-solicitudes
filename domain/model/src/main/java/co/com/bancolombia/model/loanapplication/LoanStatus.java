package co.com.bancolombia.model.loanapplication;

import lombok.Getter;

@Getter
public enum LoanStatus {
    PENDING("Pendiente"),
    REJECTED("Rechazado"),
    MANUAL_REVIEW("Revisión Manual"),
    APPROVED("Aprobado");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

}
