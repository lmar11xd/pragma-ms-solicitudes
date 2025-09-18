package co.com.bancolombia.sqs.listener;


import co.com.bancolombia.model.events.ValidationResult;
import co.com.bancolombia.usecase.processvalidation.ProcessValidationUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQSProcessorTest {

    @Mock
    private ProcessValidationUseCase processValidationUseCase;

    @InjectMocks
    private SQSProcessor sqsProcessor;

    @Test
    void shouldProcessValidMessage() {
        // given
        String body = """
            {"status":"APPROVED","documentNumber":"12345"}
            """;
        Message message = Message.builder().body(body).build();

        when(processValidationUseCase.process(any(ValidationResult.class)))
                .thenReturn(Mono.empty());

        // when
        Mono<Void> result = sqsProcessor.apply(message);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(processValidationUseCase, times(1)).process(any(ValidationResult.class));
    }

    @Test
    void shouldReturnErrorWhenMessageInvalid() {
        // given
        String invalidBody = "{invalid-json}";
        Message message = Message.builder().body(invalidBody).build();

        // when
        Mono<Void> result = sqsProcessor.apply(message);

        // then
        StepVerifier.create(result)
                .expectError(JsonProcessingException.class)
                .verify();

        verify(processValidationUseCase, never()).process(any());
    }

    @Test
    void shouldPropagateErrorFromUseCase() {
        // given
        String body = """
            {"status":"REJECTED","documentNumber":"77777"}
            """;
        Message message = Message.builder().body(body).build();

        when(processValidationUseCase.process(any(ValidationResult.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        // when
        Mono<Void> result = sqsProcessor.apply(message);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();

        verify(processValidationUseCase, times(1)).process(any(ValidationResult.class));
    }
}