package yuphy.outbox.starter.outbox;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration(after = OutboxAutoConfiguration.class)
@EnableScheduling
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherAutoConfiguration {

    @Bean
    public OutboxPublisher outboxPublisher(OutboxMessageRepository repository,
                                           KafkaTemplate<String, String> kafkaTemplate,
                                           OutboxProperties properties,
                                           Clock clock) {
        return new OutboxPublisher(repository, kafkaTemplate, properties, clock);
    }
}
