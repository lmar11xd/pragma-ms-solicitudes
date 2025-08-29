package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.config.ApplicantPropertiesConfig;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.exception.ExternalServiceException;
import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ApplicantClient implements ApplicantPort {

    private final WebClient.Builder webClient;
    private final ApplicantPropertiesConfig applicantPropertiesConfig;

    @Override
    @CircuitBreaker(name = "findApplicantByDocumentNumberCB", fallbackMethod = "fallbackFindApplicantByDocumentNumber")
    public Mono<Applicant> findApplicantByDocumentNumber(String documentNumber) {
        return webClient.build()
                .get()
                .uri(applicantPropertiesConfig.getBaseUrl() + "/document/{documentNumber}", documentNumber)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty(); // simplemente no existe applicant
                    }
                    return response.bodyToMono(Applicant.class);
                });
    }

    // Fallback cuando el circuito se abre o hay error
    private Mono<Applicant> fallbackFindApplicantByDocumentNumber(String documentNumber, Throwable ex) {
        return Mono.error(new ExternalServiceException(ErrorCode.APPLICANT_SERVICE_FAILED));
    }
}
