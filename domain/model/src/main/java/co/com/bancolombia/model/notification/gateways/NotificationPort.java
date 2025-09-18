package co.com.bancolombia.model.notification.gateways;

import co.com.bancolombia.model.notification.EventType;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    <T> Mono<String> send(T payload, EventType type);
}
