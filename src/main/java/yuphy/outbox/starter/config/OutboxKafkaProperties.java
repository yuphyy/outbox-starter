package yuphy.outbox.starter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for the outbox Kafka producer. */
@Getter
@Setter
@ConfigurationProperties(prefix = "outbox.kafka")
public class OutboxKafkaProperties {

    private boolean enabled = true;
    private String bootstrapServers;
    private String securityProtocol = "PLAINTEXT";
    private Ssl ssl = new Ssl();

    /** SSL settings for Kafka connectivity. */
    @Getter
    @Setter
    public static class Ssl {
        private String truststoreLocation;
        private String truststorePassword;
        private String truststoreType = "JKS";
        private String keystoreLocation;
        private String keystorePassword;
        private String keystoreType = "PKCS12";
        private String keyPassword;
    }
}
