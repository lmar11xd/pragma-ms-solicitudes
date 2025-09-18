package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CapacityRequest;
import co.com.bancolombia.api.dto.CreateLoanApplicationRequest;
import co.com.bancolombia.api.dto.UpdateStatusRequest;
import co.com.bancolombia.api.mapper.LoanApplicationMapper;
import co.com.bancolombia.api.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Log4j2
@Component
@RequiredArgsConstructor
public class Handler {
    private final LoanApplicationService loanApplicationService;

    public Mono<ServerResponse> create(ServerRequest serverRequest) {
        log.info("Solicitud POST::create {}", serverRequest.path());

        return serverRequest.bodyToMono(CreateLoanApplicationRequest.class)
                .flatMap(loanApplicationService::create)
                .flatMap(response -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                )
                .doOnError(error ->
                        log.error("Falló en creación de la solicitud de crédito: {}", error.getMessage(), error)
                );
    }

    public Mono<ServerResponse> listApplications(ServerRequest request) {
        log.info("Solicitud GET::listApplications {}", request.path());

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("20"));
        String statuses = request.queryParam("statuses").orElse(null);
        String loanType = request.queryParam("loanType").orElse(null);
        String documentNumber = request.queryParam("documentNumber").orElse(null);

        return loanApplicationService.listApplications(page, size, statuses, loanType, documentNumber)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .doOnError(e -> log.error("Falló listando solicitudes: {}", e.getMessage(), e));
    }

    public Mono<ServerResponse> changeStatus(ServerRequest request) {
        log.info("Solicitud PUT::changeStatus {}", request.path());

        String loanId = request.pathVariable("id");

        return request.bodyToMono(UpdateStatusRequest.class)
                .flatMap(dto -> loanApplicationService
                        .changeStatus(loanId, dto.status().name()))
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .doOnError(e -> log.error("Falló cambio de estado solicitud id={}: {}", loanId, e.getMessage(), e));

    }

    public Mono<ServerResponse> calculateCapacityDebt(ServerRequest request) {
        log.info("Solicitud POST::calculateCapacityDebt {}", request.path());

        return request.bodyToMono(CapacityRequest.class)
                .flatMap(loanApplicationService::calculateCapacityDebt)
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .doOnError(e -> log.error("Falló consultando capacidad de endeudamiento {}", e.getMessage(), e));
    }
}
