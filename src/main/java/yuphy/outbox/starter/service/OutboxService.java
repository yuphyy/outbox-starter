package yuphy.outbox.starter.service;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import yuphy.outbox.starter.api.OutboxClient;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/** Default outbox service implementation. */
@RequiredArgsConstructor
public class OutboxService implements OutboxClient {

    private final OutboxMessageRepository repository;
    private final OutboxRouteResolver routeResolver;
    private final Clock clock;

    @Transactional("outboxTransactionManager")
    @Override
    /** Enqueues a message with required type and recipient. */
    public UUID enqueue(String messageType, String recipient, String messageKey, String payload) {
        String safeType = requireText(messageType, "messageType");
        String safeRecipient = requireText(recipient, "recipient");
        String topic = routeResolver.resolveTopic(safeType, safeRecipient);
        OutboxMessage message = OutboxMessage.pending(topic, safeType, safeRecipient, messageKey, payload, clock);
        repository.save(message);
        return message.getId();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
