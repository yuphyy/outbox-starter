package yuphy.outbox.starter.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EN: Dedicated datasource properties for outbox persistence.
 * RU: Настройки отдельного datasource для outbox.
 */
@ConfigurationProperties(prefix = "outbox.datasource")
public class OutboxDataSourceProperties extends DataSourceProperties {
}
