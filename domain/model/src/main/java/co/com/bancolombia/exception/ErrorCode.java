package co.com.bancolombia.exception;

public enum ErrorCode {

    REQUERID_EMAIL("VALIDATION_REQUERID_EMAIL", "Correo electronico es obligatorio"),
    REQUERID_APPLICANTID("VALIDATION_REQUERID_APPLICANTID", "Identificador del solicitante es requerido"),
    REQUERID_DOCUMENTNUMBER("VALIDATION_REQUERID_DOCUMENTNUMBER", "Documento de identidad del solicitante es requerido"),
    INVALID_AMOUNT("VALIDATION_INVALID_AMOUNT", "Monto debe ser mayor a 0"),
    INVALID_TERMMONTHS("VALIDATION_INVALID_TERMMONTHS", "Plazo en meses debe ser mayor a 0"),
    INVALID_LOANTYPE("VALIDATION_INVALID_LOANTYPE", "Tipo de credito no valido"),
    APPLICANT_NOT_FOUND("APPLICANT_NOT_FOUND", "No se encontro solicitante con el numero de documento proporcionado"),
    APPLICANT_SERVICE_FAILED("APPLICANT_SERVICE_FAILED",  "El servicio de autenticacion no esta disponible en este momento");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
