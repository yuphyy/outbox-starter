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

@AutoConfiguration(after = OutboxAutoConfiguration.class)
@EnableConfigurationProperties(OutboxKafkaProperties.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "outbox.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "outbox.kafka", name = "bootstrap-servers")
public class OutboxKafkaAutoConfiguration {

    @Bean("outboxKafkaProducerFactory")
    public ProducerFactory<String, String> outboxKafkaProducerFactory(OutboxKafkaProperties properties) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        applySecurity(config, properties);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean("outboxKafkaTemplate")
    public KafkaTemplate<String, String> outboxKafkaTemplate(
            @Qualifier("outboxKafkaProducerFactory") ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private void applySecurity(Map<String, Object> config, OutboxKafkaProperties properties) {
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, properties.getSecurityProtocol());
        OutboxKafkaProperties.Ssl ssl = properties.getSsl();
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
