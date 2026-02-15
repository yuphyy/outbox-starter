package yuphy.outbox.starter.publisher;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import yuphy.outbox.starter.model.OutboxMessage;

@RequiredArgsConstructor
public class OutboxSender {

    private static final Logger log = LoggerFactory.getLogger(OutboxSender.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final long sendTimeoutMs;

    public boolean send(OutboxMessage message) {
        try {
            kafkaTemplate
                    .send(message.getTopic(), message.getMessageKey(), message.getPayload())
                    .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Outbox publish interrupted for message {}", message.getId(), ex);
            return false;
        } catch (Exception ex) {
            log.warn("Outbox publish failed for message {}", message.getId(), ex);
            return false;
        }
    }
}
