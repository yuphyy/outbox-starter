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

/** Core outbox auto-configuration (no Kafka). */
@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.JpaTransactionManager")
public class OutboxAutoConfiguration {

    /** Clock used for timestamps in outbox entities. */
    @Bean
    @ConditionalOnMissingBean
    public Clock outboxClock() {
        return Clock.systemUTC();
    }

    /** Resolves a Kafka topic by message type and recipient. */
    @Bean
    @ConditionalOnMissingBean
    public OutboxRouteResolver outboxRouteResolver(OutboxProperties properties) {
        return new OutboxRouteResolver(properties);
    }

    /** Outbox service used by application code. */
    @Bean
    @ConditionalOnMissingBean
    public OutboxService outboxService(OutboxMessageRepository repository,
                                       OutboxRouteResolver routeResolver,
                                       Clock clock) {
        return new OutboxService(repository, routeResolver, clock);
    }
}
