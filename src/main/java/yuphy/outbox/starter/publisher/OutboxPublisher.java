package yuphy.outbox.starter.publisher;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.model.OutboxMessage;

/**
 * EN: Scheduled publisher that sends pending outbox messages to Kafka.
 * RU: Планировщик, отправляющий ожидающие outbox-сообщения в Kafka.
 */
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxBatchReader batchReader;
    private final OutboxSender sender;
    private final OutboxProperties properties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:1000}")
    @Transactional("outboxTransactionManager")
    /**
     * EN: Sends a batch of pending messages and marks them as sent.
     * RU: Отправляет батч сообщений и помечает их как SENT.
     */
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
