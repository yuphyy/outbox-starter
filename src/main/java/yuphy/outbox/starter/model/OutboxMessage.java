package yuphy.outbox.starter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_message")
public class OutboxMessage {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String topic;

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

    @Version
    private long version;

    protected OutboxMessage() {
    }

    private OutboxMessage(UUID id, String topic, String messageKey, String payload, OutboxMessageStatus status,
                          Instant createdAt, Instant sentAt) {
        this.id = id;
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public static OutboxMessage pending(String topic, String messageKey, String payload, Clock clock) {
        return new OutboxMessage(
                UUID.randomUUID(),
                topic,
                messageKey,
                payload,
                OutboxMessageStatus.PENDING,
                Instant.now(clock),
                null
        );
    }

    public void markSent(Clock clock) {
        this.status = OutboxMessageStatus.SENT;
        this.sentAt = Instant.now(clock);
    }

    public UUID getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxMessageStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
