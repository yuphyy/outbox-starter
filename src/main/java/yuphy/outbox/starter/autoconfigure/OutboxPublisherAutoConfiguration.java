package yuphy.outbox.starter.autoconfigure;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.publisher.OutboxBatchReader;
import yuphy.outbox.starter.publisher.OutboxPublisher;
import yuphy.outbox.starter.publisher.OutboxSender;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

@AutoConfiguration(after = OutboxAutoConfiguration.class)
@EnableScheduling
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherAutoConfiguration {

    @Bean
    public OutboxBatchReader outboxBatchReader(OutboxMessageRepository repository) {
        return new OutboxBatchReader(repository);
    }

    @Bean
    public OutboxSender outboxSender(KafkaTemplate<String, String> kafkaTemplate, OutboxProperties properties) {
        return new OutboxSender(kafkaTemplate, properties.getPublisher().getSendTimeoutMs());
    }

    @Bean
    public OutboxPublisher outboxPublisher(OutboxBatchReader batchReader,
                                           OutboxSender sender,
                                           OutboxProperties properties,
                                           Clock clock) {
        return new OutboxPublisher(batchReader, sender, properties, clock);
    }
}
