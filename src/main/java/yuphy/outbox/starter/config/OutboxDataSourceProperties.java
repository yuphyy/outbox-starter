package yuphy.outbox.starter.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Dedicated datasource properties for outbox persistence. */
@ConfigurationProperties(prefix = "outbox.datasource")
public class OutboxDataSourceProperties extends DataSourceProperties {
}
