package co.com.bancolombia.api.exception;

import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ExternalServiceException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static co.com.bancolombia.api.util.Utils.toJson;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomainException(DomainException ex) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getDefaultMessage(),
                "/api/error",
                ex.getDetails()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message = ex.getMessage();
        Map<String, Object> details = Map.of();

        switch (ex) {
            case IllegalStateException illegalStateException -> status = HttpStatus.CONFLICT;
            case ResponseStatusException rse -> {
                status = HttpStatus.valueOf(rse.getStatusCode().value());
                message = rse.getReason();
            }
            case DomainException domainException -> {
                status = HttpStatus.BAD_REQUEST;
                details = domainException.getDetails();
            }
            case ExternalServiceException externalServiceException -> status = HttpStatus.SERVICE_UNAVAILABLE;
            default -> status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                details
        );

        byte[] bytes = toJson(errorResponse).getBytes(StandardCharsets.UTF_8);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBufferFactory dbf = exchange.getResponse().bufferFactory();
        return exchange.getResponse().writeWith(Mono.just(dbf.wrap(bytes)));
    }
}
