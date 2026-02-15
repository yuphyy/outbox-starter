package yuphy.outbox.starter.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private Map<String, RouteGroup> routes = new HashMap<>();
    private Publisher publisher = new Publisher();

    @Getter
    @Setter
    public static class RouteGroup {
        private Map<String, String> recipients = new HashMap<>();
    }

    @Getter
    @Setter
    public static class Publisher {
        private boolean enabled = true;
        private int batchSize = 100;
        private long pollIntervalMs = 1000;
        private long sendTimeoutMs = 5000;
    }
}
