package yuphy.outbox.starter.autoconfigure;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import yuphy.outbox.starter.config.OutboxDataSourceProperties;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

@AutoConfiguration(after = OutboxAutoConfiguration.class, before = OutboxJpaAutoConfiguration.class)
@EnableConfigurationProperties(OutboxDataSourceProperties.class)
@EntityScan(basePackageClasses = OutboxMessage.class)
@EnableJpaRepositories(
        basePackageClasses = OutboxMessageRepository.class,
        entityManagerFactoryRef = "outboxEntityManagerFactory",
        transactionManagerRef = "outboxTransactionManager"
)
@Conditional(OutboxDataSourceCondition.class)
@ConditionalOnClass({DataSource.class, LocalContainerEntityManagerFactoryBean.class})
public class OutboxDataSourceAutoConfiguration {

    @Bean("outboxDataSource")
    @ConditionalOnMissingBean(name = "outboxDataSource")
    public DataSource outboxDataSource(OutboxDataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

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

    @Bean("outboxTransactionManager")
    public PlatformTransactionManager outboxTransactionManager(
            @Qualifier("outboxEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
