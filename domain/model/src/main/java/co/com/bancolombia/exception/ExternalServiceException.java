package co.com.bancolombia.exception;

public class ExternalServiceException extends RuntimeException {
    private final ErrorCode errorCode;

    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
}