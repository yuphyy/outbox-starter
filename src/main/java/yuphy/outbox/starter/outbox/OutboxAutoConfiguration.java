package yuphy.outbox.starter.outbox;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
@EntityScan(basePackageClasses = OutboxMessage.class)
@EnableJpaRepositories(basePackageClasses = OutboxMessageRepository.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.JpaTransactionManager")
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock outboxClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxService outboxService(OutboxMessageRepository repository,
                                       OutboxProperties properties,
                                       Clock clock) {
        return new OutboxService(repository, properties, clock);
    }
}
