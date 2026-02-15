package yuphy.outbox.starter.autoconfigure;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/** Checks whether a dedicated outbox datasource is configured. */
public class OutboxDataSourceCondition implements Condition {

    /** Matches when outbox.datasource.url is provided. */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String url = environment.getProperty("outbox.datasource.url");
        return url != null && !url.isBlank();
    }
}
