package yuphy.outbox.starter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private String topic = "outbox.events";
    private Publisher publisher = new Publisher();

    @Getter
    @Setter
    public static class Publisher {
        private boolean enabled = true;
        private int batchSize = 100;
        private long pollIntervalMs = 1000;
        private long sendTimeoutMs = 5000;
    }
}
