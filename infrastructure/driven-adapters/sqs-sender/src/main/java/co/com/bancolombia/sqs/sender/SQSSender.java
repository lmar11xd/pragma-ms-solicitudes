package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.notification.Notification;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationPort {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    public Mono<String> send(Notification notification) {
        log.info("Inicia proceso para enviar Evento SQS");
        return Mono.fromCallable(() -> buildRequest(notification))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Evento SQS publicado con ID: {}", response.messageId()))
                .map(SendMessageResponse::messageId)
                .onErrorResume(ex -> {
                    log.error("Error publicando mensaje en SQS: {}", ex.getMessage(), ex);
                    return Mono.error(new DomainException(ErrorCode.SQS_SEND_ERROR, ex.getMessage()));
                });
    }

    private SendMessageRequest buildRequest(Notification notification) throws JsonProcessingException {
        log.info("Preparando mensaje para Evento SQS {}", notification.getDocumentNumber());
        String body = objectMapper.writeValueAsString(notification);
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(body)
                .build();
    }
}
