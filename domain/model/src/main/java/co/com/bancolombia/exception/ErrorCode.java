package co.com.bancolombia.exception;

public enum ErrorCode {

    REQUERID_EMAIL("VALIDATION_REQUERID_EMAIL", "Correo electronico es obligatorio"),
    REQUERID_APPLICANTID("VALIDATION_REQUERID_APPLICANTID", "Identificador del solicitante es requerido"),
    REQUERID_DOCUMENTNUMBER("VALIDATION_REQUERID_DOCUMENTNUMBER", "Documento de identidad del solicitante es requerido"),
    INVALID_AMOUNT("VALIDATION_INVALID_AMOUNT", "Monto debe ser mayor a 0"),
    INVALID_TERMMONTHS("VALIDATION_INVALID_TERMMONTHS", "Plazo en meses debe ser mayor a 0"),
    INVALID_LOANTYPE("VALIDATION_INVALID_LOANTYPE", "Tipo de credito no valido"),
    APPLICANT_NOT_FOUND("APPLICANT_NOT_FOUND", "No se encontro solicitante con el numero de documento proporcionado"),
    APPLICANT_SERVICE_FAILED("APPLICANT_SERVICE_FAILED",  "El servicio de autenticacion no esta disponible en este momento"),
    UNAUTHORIZED("UNAUTHORIZED", "No tiene autorización para acceder a este recurso."),
    FORBIDDEN("FORBIDDEN", "Acceso denegado."),
    UNAUTHORIZED_ACTION("UNAUTHORIZED_ACTION", "No puedes crear solicitudes para otro usuario"),
    INVALID_LOAN_PARAMETERS("INVALID_LOAN_PARAMETERS", "Parametros de credito invalidos"),

    DATABASE_CONSTRAINT_VIOLATION("DATABASE_CONSTRAINT_VIOLATION", "Error de clave unica"),
    DATABASE_LOCK_TIMEOUT("DATABASE_LOCK_TIMEOUT", "Error de conexion a base de datos"),
    DATABASE_ERROR("DATABASE_ERROR", "Error de base de datos"),
    INVALID_STATUS("INVALID_STATUS", "No es un estado valido para esta operacion"),
    LOAN_NOT_FOUND("LOAN_NOT_FOUND","Solicitud de credito no encontrada"),
    SQS_SEND_ERROR("SQS_SEND_ERROR", "Error con SQS");

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
