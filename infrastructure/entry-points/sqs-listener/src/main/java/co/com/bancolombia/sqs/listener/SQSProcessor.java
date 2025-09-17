package co.com.bancolombia.sqs.listener;

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
    // private final MyUseCase myUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("Mensaje recibido body={}", message.body());


        return Mono.empty();
        // return myUseCase.doAny(message.body());
    }
}
