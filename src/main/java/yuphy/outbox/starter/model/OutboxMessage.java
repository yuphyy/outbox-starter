package yuphy.outbox.starter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * EN: Outbox message persisted in the database.
 * RU: Сообщение outbox, сохраняемое в базе данных.
 */
@Getter
@Entity
@Table(name = "outbox_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OutboxMessage {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(nullable = false)
    private String recipient;

    @Column(name = "message_key")
    private String messageKey;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxMessageStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /**
     * EN: Creates a new pending outbox message.
     * RU: Создает новое сообщение со статусом PENDING.
     *
     * @param topic EN: Kafka topic. RU: Kafka-топик.
     * @param messageType EN: message type. RU: тип сообщения.
     * @param recipient EN: recipient/service name. RU: получатель/сервис.
     * @param messageKey EN: Kafka message key (nullable). RU: ключ Kafka (может быть null).
     * @param payload EN: payload string. RU: полезная нагрузка строкой.
     * @param clock EN: clock for timestamps. RU: часы для отметок времени.
     * @return EN: outbox message. RU: сообщение outbox.
     */
    public static OutboxMessage pending(String topic,
                                        String messageType,
                                        String recipient,
                                        String messageKey,
                                        String payload,
                                        Clock clock) {
        return new OutboxMessage(
                UUID.randomUUID(),
                topic,
                messageType,
                recipient,
                messageKey,
                payload,
                OutboxMessageStatus.PENDING,
                Instant.now(clock),
                null,
                0
        );
    }

    /**
     * EN: Two outbox messages are equal if they share the same id.
     * RU: Два outbox-сообщения равны, если у них одинаковый id.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxMessage other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}