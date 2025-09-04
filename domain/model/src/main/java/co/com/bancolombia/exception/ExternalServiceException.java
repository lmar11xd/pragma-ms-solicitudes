package co.com.bancolombia.exception;

public class ExternalServiceException extends RuntimeException {
    public final ErrorCode errorCode;

    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
}