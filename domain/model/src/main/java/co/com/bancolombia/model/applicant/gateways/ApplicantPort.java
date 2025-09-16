package co.com.bancolombia.model.applicant.gateways;

import co.com.bancolombia.model.applicant.Applicant;
import reactor.core.publisher.Mono;

public interface ApplicantPort {
    Mono<Applicant> findApplicantByDocumentNumber(String documentNumber);
}
