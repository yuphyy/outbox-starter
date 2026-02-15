package yuphy.outbox.starter.api;

import java.util.UUID;

/**
 * EN: Public outbox API used by application code.
 * RU: Публичный API outbox, используемый прикладным кодом.
 */
public interface OutboxClient {

    /**
     * EN: Enqueue a message for outbox delivery.
     * RU: Сохранить сообщение в outbox для последующей отправки.
     *
     * @param messageType EN: message type identifier. RU: тип сообщения.
     * @param recipient EN: logical recipient/service name. RU: получатель/сервис.
     * @param messageKey EN: Kafka message key (nullable). RU: ключ Kafka (может быть null).
     * @param payload EN: payload as string. RU: полезная нагрузка в виде строки.
     * @return EN: outbox message id. RU: идентификатор сообщения в outbox.
     */
    UUID enqueue(String messageType, String recipient, String messageKey, String payload);
}
