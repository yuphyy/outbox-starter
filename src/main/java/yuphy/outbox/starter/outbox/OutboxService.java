package yuphy.outbox.starter.outbox;

import java.time.Clock;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class OutboxService {

    private final OutboxMessageRepository repository;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxService(OutboxMessageRepository repository, OutboxProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public UUID enqueue(String messageKey, String payload) {
        OutboxMessage message = OutboxMessage.pending(properties.getTopic(), messageKey, payload, clock);
        repository.save(message);
        return message.getId();
    }
}
