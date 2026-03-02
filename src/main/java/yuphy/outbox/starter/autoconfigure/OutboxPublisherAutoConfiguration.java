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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.publisher.OutboxBatchReader;
import yuphy.outbox.starter.publisher.OutboxPublisher;
import yuphy.outbox.starter.publisher.OutboxSender;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/**
 * EN: Auto-configuration for the outbox publisher scheduler.
 * RU: Автоконфигурация планировщика публикации outbox.
 */
@AutoConfiguration(after = {OutboxAutoConfiguration.class, OutboxKafkaAutoConfiguration.class})
@EnableScheduling
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean(name = "outboxKafkaTemplate")
@ConditionalOnProperty(prefix = "outbox.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherAutoConfiguration {

    /**
     * EN: Reads pending outbox messages in batches.
     * RU: Читает ожидающие outbox-сообщения батчами.
     *
     * @param repository EN: outbox repository. RU: репозиторий outbox.
     * @return EN: batch reader. RU: читатель батчей.
     */
    @Bean
    public OutboxBatchReader outboxBatchReader(OutboxMessageRepository repository) {
        return new OutboxBatchReader(repository);
    }

    /**
     * EN: Sends a single outbox message to Kafka.
     * RU: Отправляет одно outbox-сообщение в Kafka.
     *
     * @param kafkaTemplate EN: outbox Kafka template. RU: Kafka шаблон outbox.
     * @param properties EN: outbox properties. RU: настройки outbox.
     * @return EN: sender. RU: отправитель.
     */
    @Bean
    public OutboxSender outboxSender(@Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                                     OutboxProperties properties) {
        long sendTimeoutMs = properties.getPublisher().getSendTimeoutMs();
        if (sendTimeoutMs <= 0) {
            throw new IllegalArgumentException(
                    "outbox.publisher.send-timeout-ms must be positive, got: " + sendTimeoutMs);
        }
        return new OutboxSender(kafkaTemplate, sendTimeoutMs);
    }

    /**
     * EN: Scheduled publisher that marks messages as sent.
     * RU: Планировщик, публикующий сообщения и помечающий их как SENT.
     *
     * @param batchReader EN: batch reader. RU: читатель батчей.
     * @param sender EN: sender. RU: отправитель.
     * @param properties EN: outbox properties. RU: настройки outbox.
     * @param clock EN: clock for timestamps. RU: часы для отметок времени.
     * @param transactionManager EN: outbox transaction manager. RU: менеджер транзакций outbox.
     * @param repository EN: outbox repository. RU: репозиторий outbox.
     * @return EN: publisher. RU: паблишер.
     */
    @Bean
    public OutboxPublisher outboxPublisher(OutboxBatchReader batchReader,
                                           OutboxSender sender,
                                           OutboxProperties properties,
                                           Clock clock,
                                           @Qualifier("outboxTransactionManager") PlatformTransactionManager transactionManager,
                                           OutboxMessageRepository repository) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return new OutboxPublisher(batchReader, sender, properties, clock, transactionTemplate, repository);
    }
}
