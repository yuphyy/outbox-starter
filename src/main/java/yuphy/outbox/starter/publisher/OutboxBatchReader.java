package yuphy.outbox.starter.publisher;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.model.OutboxMessageStatus;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

public class OutboxBatchReader {

    private final OutboxMessageRepository repository;

    public OutboxBatchReader(OutboxMessageRepository repository) {
        this.repository = repository;
    }

    public List<OutboxMessage> loadPending(int batchSize) {
        return repository.findBatchForUpdate(
                OutboxMessageStatus.PENDING,
                PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "createdAt"))
        );
    }
}
