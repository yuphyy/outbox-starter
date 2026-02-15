package yuphy.outbox.starter.autoconfigure;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.repository.OutboxMessageRepository;
import yuphy.outbox.starter.service.OutboxRouteResolver;
import yuphy.outbox.starter.service.OutboxService;

/**
 * EN: Core outbox auto-configuration (no Kafka).
 * RU: Базовая автоконфигурация outbox (без Kafka).
 */
@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.JpaTransactionManager")
public class OutboxAutoConfiguration {

    /**
     * EN: Clock used for timestamps in outbox entities.
     * RU: Часы для отметок времени в outbox-сущностях.
     *
     * @return EN: system UTC clock. RU: системные UTC часы.
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock outboxClock() {
        return Clock.systemUTC();
    }

    /**
     * EN: Resolves a Kafka topic by message type and recipient.
     * RU: Выбирает Kafka-топик по типу сообщения и получателю.
     *
     * @param properties EN: outbox properties. RU: настройки outbox.
     * @return EN: route resolver. RU: резолвер маршрутов.
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxRouteResolver outboxRouteResolver(OutboxProperties properties) {
        return new OutboxRouteResolver(properties);
    }

    /**
     * EN: Outbox service used by application code.
     * RU: Сервис outbox для использования в приложении.
     *
     * @param repository EN: outbox repository. RU: репозиторий outbox.
     * @param routeResolver EN: route resolver. RU: резолвер маршрутов.
     * @param clock EN: clock for timestamps. RU: часы для отметок времени.
     * @return EN: outbox service. RU: сервис outbox.
     */
    @Bean
    @ConditionalOnMissingBean
    public OutboxService outboxService(OutboxMessageRepository repository,
                                       OutboxRouteResolver routeResolver,
                                       Clock clock) {
        return new OutboxService(repository, routeResolver, clock);
    }
}
