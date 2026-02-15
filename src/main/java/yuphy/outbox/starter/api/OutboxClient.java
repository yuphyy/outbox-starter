package yuphy.outbox.starter.api;

import java.util.UUID;

public interface OutboxClient {

    UUID enqueue(String messageKey, String payload);
}
