package yuphy.outbox.starter.service;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import yuphy.outbox.starter.api.OutboxClient;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/**
 * EN: Default outbox service implementation.
 * RU: Базовая реализация outbox-сервиса.
 */
@RequiredArgsConstructor
public class OutboxService implements OutboxClient {

    private final OutboxMessageRepository repository;
    private final OutboxRouteResolver routeResolver;
    private final Clock clock;

    /**
     * EN: Enqueues a message with required type and recipient.
     * RU: Сохраняет сообщение с обязательными type и recipient.
     *
     * @param messageType EN: message type. RU: тип сообщения.
     * @param recipient EN: recipient/service name. RU: получатель/сервис.
     * @param messageKey EN: Kafka message key (nullable). RU: ключ Kafka (может быть null).
     * @param payload EN: payload string. RU: полезная нагрузка строкой.
     * @return EN: outbox message id. RU: идентификатор сообщения outbox.
     */
    @Transactional("outboxTransactionManager")
    @Override
    public UUID enqueue(String messageType, String recipient, String messageKey, String payload) {
        String safeType = requireText(messageType, "messageType");
        String safeRecipient = requireText(recipient, "recipient");
        String topic = routeResolver.resolveTopic(safeType, safeRecipient);
        OutboxMessage message = OutboxMessage.pending(topic, safeType, safeRecipient, messageKey, payload, clock);
        repository.save(message);
        return message.getId();
    }

    /**
     * EN: Validates that the value is not blank.
     * RU: Проверяет, что значение не пустое.
     *
     * @param value EN: value to check. RU: проверяемое значение.
     * @param name EN: parameter name. RU: имя параметра.
     * @return EN: same value if valid. RU: то же значение, если валидно.
     */
    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
