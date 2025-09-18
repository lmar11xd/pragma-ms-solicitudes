package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.notification.EventType;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationPort {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    public <T> Mono<String> send(T payload, EventType type) {
        log.info("Inicia proceso para enviar Evento SQS, eventType={}", type);

        return Mono.fromCallable(() -> buildRequest(payload, type))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Evento SQS publicado con ID: {}", response.messageId()))
                .map(SendMessageResponse::messageId)
                .onErrorResume(ex -> {
                    log.error("Error publicando mensaje en SQS: {}", ex.getMessage(), ex);
                    return Mono.error(new DomainException(ErrorCode.SQS_SEND_ERROR, ex.getMessage()));
                });
    }

    private <T> SendMessageRequest buildRequest(T payload, EventType type) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(payload);

        log.info("Preparando mensaje para Evento SQS: body={}, eventType={}", body, type);

        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(body)
                .messageAttributes(
                        Map.of(
                                "eventType",
                                MessageAttributeValue
                                        .builder()
                                        .dataType("String")
                                        .stringValue(type.name())
                                        .build()
                        )
                )
                .build();
    }
}
