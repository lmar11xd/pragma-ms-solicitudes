package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.config.ApplicantPropertiesConfig;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.exception.ExternalServiceException;
import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.security.SecurityPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicantClient implements ApplicantPort {

    private final WebClient.Builder webClient;
    private final ApplicantPropertiesConfig applicantPropertiesConfig;
    private final SecurityPort securityPort;

    @Override
    @CircuitBreaker(name = "findApplicantByDocumentNumberCB", fallbackMethod = "fallbackFindApplicantByDocumentNumber")
    public Mono<Applicant> findApplicantByDocumentNumber(String documentNumber) {
        log.info("Iniciando búsqueda de solicitante con documento {}", documentNumber);

        return securityPort.getCurrentUserToken()
                .switchIfEmpty(Mono.error(new ExternalServiceException(ErrorCode.UNAUTHORIZED)))
                .flatMap(token -> webClient.build()
                        .get()
                        .uri(applicantPropertiesConfig.getBaseUrl() + "/document/{documentNumber}", documentNumber)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .exchangeToMono(response -> {
                            HttpStatus status = (HttpStatus) response.statusCode();

                            if (status.equals(HttpStatus.NOT_FOUND)) {
                                return Mono.empty(); // simplemente no existe applicant
                            } else if (status.equals(HttpStatus.UNAUTHORIZED)) {
                                return Mono.error(new ExternalServiceException(ErrorCode.UNAUTHORIZED));
                            } else if (status.equals(HttpStatus.FORBIDDEN)) {
                                return Mono.error(new ExternalServiceException(ErrorCode.FORBIDDEN));
                            } else if (status.is5xxServerError()) {
                                return Mono.error(new ExternalServiceException(ErrorCode.APPLICANT_SERVICE_FAILED));
                            }

                            return response.bodyToMono(Applicant.class);
                        })
                );
    }

    // Fallback cuando el circuito se abre o hay error
    private Mono<Applicant> fallbackFindApplicantByDocumentNumber(String documentNumber, Throwable ex) {
        return Mono.error(ex);
    }
}
