package yuphy.outbox.starter.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for outbox routing and publisher. */
@Getter
@Setter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private Map<String, RouteGroup> routes = new HashMap<>();
    private Publisher publisher = new Publisher();

    /** Routes grouped by message type. */
    @Getter
    @Setter
    public static class RouteGroup {
        private Map<String, String> recipients = new HashMap<>();
    }

    /** Publisher scheduling and batching settings. */
    @Getter
    @Setter
    public static class Publisher {
        private boolean enabled = true;
        private int batchSize = 100;
        private long pollIntervalMs = 1000;
        private long sendTimeoutMs = 5000;
    }
}
