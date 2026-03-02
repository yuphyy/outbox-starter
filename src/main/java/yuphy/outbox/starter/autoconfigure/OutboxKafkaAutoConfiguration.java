package yuphy.outbox.starter.autoconfigure;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import yuphy.outbox.starter.config.OutboxKafkaProperties;
import yuphy.outbox.starter.config.OutboxProperties;

/**
 * EN: Auto-configuration for the dedicated outbox Kafka producer.
 * RU: Автоконфигурация выделенного Kafka-продюсера для outbox.
 */
@AutoConfiguration(after = OutboxAutoConfiguration.class)
@EnableConfigurationProperties(OutboxKafkaProperties.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "outbox.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "outbox.kafka", name = "bootstrap-servers")
public class OutboxKafkaAutoConfiguration {

    /**
     * EN: Producer factory for the outbox Kafka template.
     * RU: Фабрика продюсера для Kafka шаблона outbox.
     *
     * @param kafkaProperties EN: outbox Kafka properties. RU: настройки Kafka для outbox.
     * @param outboxProperties EN: outbox properties (used to align delivery timeout). RU: настройки outbox (для выравнивания таймаута доставки).
     * @return EN: producer factory. RU: фабрика продюсера.
     */
    @Bean("outboxKafkaProducerFactory")
    public ProducerFactory<String, String> outboxKafkaProducerFactory(OutboxKafkaProperties kafkaProperties,
                                                                       OutboxProperties outboxProperties) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        int timeoutMs = (int) Math.min(outboxProperties.getPublisher().getSendTimeoutMs(), Integer.MAX_VALUE);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, timeoutMs);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, timeoutMs);
        applySecurity(config, kafkaProperties);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * EN: Kafka template dedicated to outbox publishing.
     * RU: Kafka шаблон, используемый для публикации outbox.
     *
     * @param producerFactory EN: outbox producer factory. RU: фабрика продюсера outbox.
     * @return EN: Kafka template. RU: Kafka шаблон.
     */
    @Bean("outboxKafkaTemplate")
    public KafkaTemplate<String, String> outboxKafkaTemplate(
            @Qualifier("outboxKafkaProducerFactory") ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * EN: Applies security and SSL settings to the Kafka config.
     * RU: Применяет настройки безопасности и SSL к Kafka конфигурации.
     *
     * @param config EN: Kafka config map. RU: карта настроек Kafka.
     * @param kafkaProperties EN: outbox Kafka properties. RU: настройки Kafka для outbox.
     */
    private void applySecurity(Map<String, Object> config, OutboxKafkaProperties kafkaProperties) {
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaProperties.getSecurityProtocol());
        OutboxKafkaProperties.Ssl ssl = kafkaProperties.getSsl();
        if (ssl == null) {
            return;
        }
        if (ssl.getTruststoreLocation() != null) {
            config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ssl.getTruststoreLocation());
        }
        if (ssl.getTruststorePassword() != null) {
            config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, ssl.getTruststorePassword());
        }
        if (ssl.getTruststoreType() != null) {
            config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, ssl.getTruststoreType());
        }
        if (ssl.getKeystoreLocation() != null) {
            config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ssl.getKeystoreLocation());
        }
        if (ssl.getKeystorePassword() != null) {
            config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, ssl.getKeystorePassword());
        }
        if (ssl.getKeystoreType() != null) {
            config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, ssl.getKeystoreType());
        }
        if (ssl.getKeyPassword() != null) {
            config.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, ssl.getKeyPassword());
        }
    }
}
