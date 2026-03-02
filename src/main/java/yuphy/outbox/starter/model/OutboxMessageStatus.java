package yuphy.outbox.starter.model;

/**
 * EN: Outbox delivery status.
 * RU: Статус доставки outbox.
 */
public enum OutboxMessageStatus {
    PENDING,
    SENT,
    FAILED
}
