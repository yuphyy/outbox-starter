package yuphy.outbox.starter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EN: Configuration properties for the outbox Kafka producer.
 * RU: Настройки Kafka-продюсера для outbox.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "outbox.kafka")
public class OutboxKafkaProperties {

    private boolean enabled = true;
    private String bootstrapServers;
    private String securityProtocol = "PLAINTEXT";
    private Ssl ssl = new Ssl();

    /**
     * EN: SSL settings for Kafka connectivity.
     * RU: Настройки SSL для подключения к Kafka.
     */
    @Getter
    @Setter
    public static class Ssl {
        private String truststoreLocation;
        private String truststorePassword;
        private String truststoreType = "PKCS12";
        private String keystoreLocation;
        private String keystorePassword;
        private String keystoreType = "PKCS12";
        private String keyPassword;
    }
}
