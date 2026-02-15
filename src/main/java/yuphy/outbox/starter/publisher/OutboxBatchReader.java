package yuphy.outbox.starter.publisher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.model.OutboxMessageStatus;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/** Loads pending outbox messages in batches. */
@RequiredArgsConstructor
public class OutboxBatchReader {

    private static final String CREATION_DATE_FIELD_NAME = "createdAt";

    private final OutboxMessageRepository repository;

    /** Returns a batch of pending messages with a write lock. */
    public List<OutboxMessage> loadPending(int batchSize) {
        return repository.findBatchForUpdate(
                OutboxMessageStatus.PENDING,
                PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, CREATION_DATE_FIELD_NAME))
        );
    }
}
