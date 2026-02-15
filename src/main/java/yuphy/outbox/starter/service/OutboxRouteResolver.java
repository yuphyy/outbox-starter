package yuphy.outbox.starter.service;

import lombok.RequiredArgsConstructor;
import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.config.OutboxProperties.RouteGroup;

/** Resolves a Kafka topic based on message type and recipient. */
@RequiredArgsConstructor
public class OutboxRouteResolver {

    private final OutboxProperties properties;

    /** Returns the configured topic for messageType and recipient. */
    public String resolveTopic(String messageType, String recipient) {
        RouteGroup group = properties.getRoutes().get(messageType);
        if (group == null || group.getRecipients() == null) {
            throw new IllegalArgumentException("No outbox route for type=" + messageType);
        }
        String topic = group.getRecipients().get(recipient);
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("No outbox route for type=" + messageType + " recipient=" + recipient);
        }
        return topic;
    }
}
