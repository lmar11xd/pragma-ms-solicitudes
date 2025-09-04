package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.AdvisorReviewResponse;
import co.com.bancolombia.api.dto.CreateLoanApplicationRequest;
import co.com.bancolombia.api.mapper.LoanApplicationMapper;
import co.com.bancolombia.usecase.loanapplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final LoanApplicationUseCase loanApplicationUseCase;

    private final TransactionalOperator tx;

    public Mono<ServerResponse> create(ServerRequest serverRequest) {
        log.info("Solicitud POST={}", serverRequest.path());

        return serverRequest.bodyToMono(CreateLoanApplicationRequest.class)
                .flatMap(dto -> loanApplicationUseCase.create(LoanApplicationMapper.toDomain(dto)))
                .map(saved -> {
                            log.info("Solicitud de credito creada con id {}", saved.getId());
                            return LoanApplicationMapper.toDto(saved);
                        }
                )
                .as(tx::transactional)
                .flatMap(response -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                )
                .doOnError(error ->
                        log.error("Fallo en creacion de la solicitud de credito: {}", error.getMessage(), error)
                );
    }

    public Mono<ServerResponse> listPendingApplications(ServerRequest request) {
        log.info("Solicitud GET {}", request.path());

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("20"));
        String loanType = request.queryParam("loanType").orElse(null);
        String documentNumber = request.queryParam("documentNumber").orElse(null);

        return loanApplicationUseCase.listPendingApplications(page, size, loanType, documentNumber)
                .map(pr -> AdvisorReviewResponse.builder()
                        .content(pr.content())
                        .totalElements(pr.totalElements())
                        .page(pr.page())
                        .size(pr.size())
                        .build())
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .doOnError(e -> log.error("Fallo listando solicitudes para asesor: {}", e.getMessage(), e));
    }
}
