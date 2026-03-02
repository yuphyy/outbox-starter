package yuphy.outbox.starter.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EN: Configuration properties for outbox routing and publisher.
 * RU: Настройки маршрутизации outbox и параметров паблишера.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private Map<String, RouteGroup> routes = new HashMap<>();
    private Publisher publisher = new Publisher();

    /**
     * EN: Routes grouped by message type.
     * RU: Маршруты, сгруппированные по типу сообщения.
     */
    @Getter
    @Setter
    public static class RouteGroup {
        private Map<String, String> recipients = new HashMap<>();
    }

    /**
     * EN: Publisher scheduling and batching settings.
     * RU: Настройки расписания и батчинга паблишера.
     */
    @Getter
    @Setter
    public static class Publisher {
        private boolean enabled = true;
        private int batchSize = 100;
        private long sendTimeoutMs = 5000;
        private int maxRetries = 5;
    }
}
