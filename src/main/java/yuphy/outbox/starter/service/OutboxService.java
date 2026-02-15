package yuphy.outbox.starter.service;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import yuphy.outbox.starter.api.OutboxClient;
import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

@RequiredArgsConstructor
public class OutboxService implements OutboxClient {

    private final OutboxMessageRepository repository;
    private final OutboxProperties properties;
    private final Clock clock;

    @Transactional("outboxTransactionManager")
    @Override
    public UUID enqueue(String messageKey, String payload) {
        OutboxMessage message = OutboxMessage.pending(properties.getTopic(), messageKey, payload, clock);
        repository.save(message);
        return message.getId();
    }
}
