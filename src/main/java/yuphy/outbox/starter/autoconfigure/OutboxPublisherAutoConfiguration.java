package yuphy.outbox.starter.autoconfigure;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.publisher.OutboxBatchReader;
import yuphy.outbox.starter.publisher.OutboxPublisher;
import yuphy.outbox.starter.publisher.OutboxSender;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/** Auto-configuration for the outbox publisher scheduler. */
@AutoConfiguration(after = {OutboxAutoConfiguration.class, OutboxKafkaAutoConfiguration.class})
@EnableScheduling
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean(name = "outboxKafkaTemplate")
@ConditionalOnProperty(prefix = "outbox.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherAutoConfiguration {

    /** Reads pending outbox messages in batches. */
    @Bean
    public OutboxBatchReader outboxBatchReader(OutboxMessageRepository repository) {
        return new OutboxBatchReader(repository);
    }

    /** Sends a single outbox message to Kafka. */
    @Bean
    public OutboxSender outboxSender(@Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                                     OutboxProperties properties) {
        return new OutboxSender(kafkaTemplate, properties.getPublisher().getSendTimeoutMs());
    }

    /** Scheduled publisher that marks messages as sent. */
    @Bean
    public OutboxPublisher outboxPublisher(OutboxBatchReader batchReader,
                                           OutboxSender sender,
                                           OutboxProperties properties,
                                           Clock clock) {
        return new OutboxPublisher(batchReader, sender, properties, clock);
    }
}
