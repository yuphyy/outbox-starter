package yuphy.outbox.starter.api;

import java.util.UUID;

/** Public outbox API used by application code. */
public interface OutboxClient {

    /** Enqueue a message for outbox delivery. */
    UUID enqueue(String messageType, String recipient, String messageKey, String payload);
}
