package co.com.bancolombia.api.exception;

public class JsonProcessingRuntimeException extends RuntimeException {
    public JsonProcessingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
