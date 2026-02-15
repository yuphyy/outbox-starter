package yuphy.outbox.starter.outbox;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxMessageRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxPublisher(OutboxMessageRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           OutboxProperties properties,
                           Clock clock) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        int batchSize = properties.getPublisher().getBatchSize();
        List<OutboxMessage> batch = repository.findBatchForUpdate(
                OutboxMessageStatus.PENDING,
                PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        if (batch.isEmpty()) {
            return;
        }

        for (OutboxMessage message : batch) {
            try {
                kafkaTemplate
                        .send(message.getTopic(), message.getMessageKey(), message.getPayload())
                        .get(properties.getPublisher().getSendTimeoutMs(), TimeUnit.MILLISECONDS);
                message.markSent(clock);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Outbox publish interrupted for message {}", message.getId(), ex);
            } catch (Exception ex) {
                log.warn("Outbox publish failed for message {}", message.getId(), ex);
            }
        }
    }
}
