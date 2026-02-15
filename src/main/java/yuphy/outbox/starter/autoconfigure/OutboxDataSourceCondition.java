package yuphy.outbox.starter.autoconfigure;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * EN: Checks whether a dedicated outbox datasource is configured.
 * RU: Проверяет, задан ли отдельный outbox datasource.
 */
public class OutboxDataSourceCondition implements Condition {

    /**
     * EN: Matches when outbox.datasource.url is provided.
     * RU: Возвращает true, если задан outbox.datasource.url.
     *
     * @param context EN: condition context. RU: контекст условия.
     * @param metadata EN: annotated metadata. RU: метаданные аннотаций.
     * @return EN: true if outbox datasource is configured. RU: true, если outbox datasource задан.
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String url = environment.getProperty("outbox.datasource.url");
        return url != null && !url.isBlank();
    }
}
