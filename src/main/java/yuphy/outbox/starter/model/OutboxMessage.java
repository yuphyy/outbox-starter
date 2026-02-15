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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Outbox message persisted in the database. */
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

    @Version
    private long version;

    /** Creates a new pending outbox message. */
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
                0L
        );
    }

    /** Marks message as sent. */
    public void markSent(Clock clock) {
        this.status = OutboxMessageStatus.SENT;
        this.sentAt = Instant.now(clock);
    }
}
