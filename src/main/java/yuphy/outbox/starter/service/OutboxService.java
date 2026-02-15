package yuphy.outbox.starter.service;

import java.time.Clock;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import yuphy.outbox.starter.api.OutboxClient;
import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

public class OutboxService implements OutboxClient {

    private final OutboxMessageRepository repository;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxService(OutboxMessageRepository repository, OutboxProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    @Override
    public UUID enqueue(String messageKey, String payload) {
        OutboxMessage message = OutboxMessage.pending(properties.getTopic(), messageKey, payload, clock);
        repository.save(message);
        return message.getId();
    }
}
