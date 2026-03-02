package yuphy.outbox.starter.autoconfigure;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import yuphy.outbox.starter.config.OutboxDataSourceProperties;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/**
 * EN: Auto-configuration for a dedicated outbox DataSource and JPA setup.
 * RU: Автоконфигурация выделенного DataSource и JPA для outbox.
 */
@AutoConfiguration(after = OutboxAutoConfiguration.class, before = OutboxJpaAutoConfiguration.class)
@EnableConfigurationProperties(OutboxDataSourceProperties.class)
@EnableJpaRepositories(
        basePackageClasses = OutboxMessageRepository.class,
        entityManagerFactoryRef = "outboxEntityManagerFactory",
        transactionManagerRef = "outboxTransactionManager"
)
@ConditionalOnProperty(prefix = "outbox.datasource", name = "url")
@ConditionalOnClass({DataSource.class, LocalContainerEntityManagerFactoryBean.class})
public class OutboxDataSourceAutoConfiguration {

    /**
     * EN: DataSource used by the outbox persistence unit.
     * RU: DataSource для outbox persistence unit.
     *
     * @param properties EN: outbox datasource properties. RU: настройки outbox datasource.
     * @return EN: data source. RU: источник данных.
     */
    @Bean("outboxDataSource")
    @ConditionalOnMissingBean(name = "outboxDataSource")
    public DataSource outboxDataSource(OutboxDataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * EN: Entity manager factory for outbox entities.
     * RU: Фабрика EntityManager для outbox-сущностей.
     *
     * @param builder EN: entity manager factory builder. RU: builder фабрики EntityManager.
     * @param dataSource EN: outbox datasource. RU: datasource outbox.
     * @return EN: entity manager factory bean. RU: bean фабрики EntityManager.
     */
    @Bean("outboxEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean outboxEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("outboxDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(OutboxMessage.class)
                .persistenceUnit("outbox")
                .build();
    }

    /**
     * EN: Transaction manager for outbox operations.
     * RU: Менеджер транзакций для операций outbox.
     *
     * @param entityManagerFactory EN: outbox entity manager factory. RU: фабрика EntityManager outbox.
     * @return EN: transaction manager. RU: менеджер транзакций.
     */
    @Bean("outboxTransactionManager")
    public PlatformTransactionManager outboxTransactionManager(
            @Qualifier("outboxEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
