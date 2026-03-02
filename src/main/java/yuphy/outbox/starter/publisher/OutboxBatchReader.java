package yuphy.outbox.starter.publisher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.model.OutboxMessageStatus;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/**
 * EN: Loads pending outbox messages in batches.
 * RU: Загружает ожидающие outbox-сообщения батчами.
 */
@RequiredArgsConstructor
public class OutboxBatchReader {

    private final OutboxMessageRepository repository;

    /**
     * EN: Returns a batch of pending messages with a write lock.
     * RU: Возвращает батч ожидающих сообщений с блокировкой записи.
     *
     * @param batchSize EN: max batch size. RU: максимальный размер батча.
     * @return EN: list of messages. RU: список сообщений.
     */
    public List<OutboxMessage> loadPending(int batchSize) {
        return repository.findBatchForUpdate(
                OutboxMessageStatus.PENDING,
                PageRequest.of(0, batchSize)
        );
    }
}
