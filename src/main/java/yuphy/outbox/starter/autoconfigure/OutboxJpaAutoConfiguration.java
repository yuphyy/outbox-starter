package yuphy.outbox.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/**
 * EN: Auto-configuration using the application's primary DataSource for outbox.
 * RU: Автоконфигурация outbox на основе основного DataSource приложения.
 */
@AutoConfiguration(after = OutboxDataSourceAutoConfiguration.class)
@EntityScan(basePackageClasses = OutboxMessage.class)
@EnableJpaRepositories(basePackageClasses = OutboxMessageRepository.class)
@ConditionalOnClass(name = "org.springframework.orm.jpa.JpaTransactionManager")
@ConditionalOnMissingBean(name = "outboxDataSource")
public class OutboxJpaAutoConfiguration {

    /**
     * EN: Exposes the primary transaction manager under the outbox name.
     * RU: Публикует основной менеджер транзакций под именем outbox.
     *
     * @param transactionManager EN: primary transaction manager. RU: основной менеджер транзакций.
     * @return EN: transaction manager. RU: менеджер транзакций.
     */
    @Bean("outboxTransactionManager")
    @ConditionalOnMissingBean(name = "outboxTransactionManager")
    public PlatformTransactionManager outboxTransactionManager(PlatformTransactionManager transactionManager) {
        return transactionManager;
    }
}
