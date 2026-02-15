package yuphy.outbox.starter.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbox.datasource")
public class OutboxDataSourceProperties extends DataSourceProperties {
}
