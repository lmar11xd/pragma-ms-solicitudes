package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.notification.Notification;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SQSSenderTest {

    private SQSSenderProperties properties;
    private SqsAsyncClient client;
    private ObjectMapper objectMapper;
    private SQSSender sender;

    @BeforeEach
    void setup() {
        properties = new SQSSenderProperties("us-east-1", "http://localhost:4566/queue/test-queue", null);
        client = mock(SqsAsyncClient.class);
        objectMapper = new ObjectMapper();
        sender = new SQSSender(properties, client, objectMapper);
    }

    @Test
    void shouldSendMessageSuccessfully() {
        // Arrange
        Notification notification = Notification.builder()
                .documentNumber("12345")
                .email("test@test.com")
                .build();

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId("msg-123")
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        // Act & Assert
        StepVerifier.create(sender.send(notification))
                .expectNext("msg-123")
                .verifyComplete();

        verify(client, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldFailWhenJsonProcessingException() throws Exception {
        // Arrange
        Notification notification = Notification.builder().documentNumber("9999").build();

        ObjectMapper mockMapper = mock(ObjectMapper.class);
        sender = new SQSSender(properties, client, mockMapper);

        when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {});

        // Act & Assert
        StepVerifier.create(sender.send(notification))
                .expectErrorSatisfies(ex -> {
                    assert(ex instanceof DomainException);
                    assert(((DomainException) ex).getErrorCode() == ErrorCode.SQS_SEND_ERROR);
                })
                .verify();
    }

    @Test
    void shouldFailWhenSqsClientThrowsError() {
        // Arrange
        Notification notification = Notification.builder().documentNumber("12345").build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("SQS unavailable")));

        // Act & Assert
        StepVerifier.create(sender.send(notification))
                .expectErrorSatisfies(ex -> {
                    assert(ex instanceof DomainException);
                    assert(((DomainException) ex).getErrorCode() == ErrorCode.SQS_SEND_ERROR);
                    assert(ex.getMessage().contains("SQS unavailable"));
                })
                .verify();
    }
}