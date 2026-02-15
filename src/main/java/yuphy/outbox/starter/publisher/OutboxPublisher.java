package yuphy.outbox.starter.publisher;

import java.time.Clock;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.model.OutboxMessage;

public class OutboxPublisher {

    private final OutboxBatchReader batchReader;
    private final OutboxSender sender;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxPublisher(OutboxBatchReader batchReader,
                           OutboxSender sender,
                           OutboxProperties properties,
                           Clock clock) {
        this.batchReader = batchReader;
        this.sender = sender;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxMessage> batch = batchReader.loadPending(properties.getPublisher().getBatchSize());

        if (batch.isEmpty()) {
            return;
        }

        for (OutboxMessage message : batch) {
            if (sender.send(message)) {
                message.markSent(clock);
            }
        }
    }
}
