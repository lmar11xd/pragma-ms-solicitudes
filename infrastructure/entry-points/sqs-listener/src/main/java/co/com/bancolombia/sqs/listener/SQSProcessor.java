package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.events.ValidationResult;
import co.com.bancolombia.usecase.processvalidation.ProcessValidationUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Log4j2
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final ProcessValidationUseCase processValidationUseCase;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public Mono<Void> apply(Message message) {
        log.info("Mensaje recibido body={}", message.body());

        ValidationResult result;
        try {
            result = mapper.readValue(message.body(), ValidationResult.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializando mensaje: {}", e.getMessage());
            return Mono.error(e);
        }

        return processValidationUseCase.process(result).then();
    }
}
