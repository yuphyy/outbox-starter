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

@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.JpaTransactionManager")
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock outboxClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxRouteResolver outboxRouteResolver(OutboxProperties properties) {
        return new OutboxRouteResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxService outboxService(OutboxMessageRepository repository,
                                       OutboxRouteResolver routeResolver,
                                       Clock clock) {
        return new OutboxService(repository, routeResolver, clock);
    }
}
