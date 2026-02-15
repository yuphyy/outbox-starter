package yuphy.outbox.starter.service;

import lombok.RequiredArgsConstructor;
import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.config.OutboxProperties.RouteGroup;

/**
 * EN: Resolves a Kafka topic based on message type and recipient.
 * RU: Выбирает Kafka-топик по типу сообщения и получателю.
 */
@RequiredArgsConstructor
public class OutboxRouteResolver {

    private final OutboxProperties properties;

    /**
     * EN: Returns the configured topic for messageType and recipient.
     * RU: Возвращает настроенный топик для messageType и recipient.
     *
     * @param messageType EN: message type. RU: тип сообщения.
     * @param recipient EN: recipient/service name. RU: получатель/сервис.
     * @return EN: Kafka topic. RU: Kafka-топик.
     */
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
