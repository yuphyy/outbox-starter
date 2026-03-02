package yuphy.outbox.starter.publisher;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import yuphy.outbox.starter.config.OutboxProperties;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.model.OutboxMessageStatus;
import yuphy.outbox.starter.repository.OutboxMessageRepository;

/**
 * EN: Scheduled publisher that sends pending outbox messages to Kafka.
 * RU: Планировщик, отправляющий ожидающие outbox-сообщения в Kafka.
 */
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxBatchReader batchReader;
    private final OutboxSender sender;
    private final OutboxProperties properties;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;
    private final OutboxMessageRepository repository;

    /**
     * EN: Sends a batch of pending messages and marks them as sent.
     * Runs in three phases to avoid holding a DB connection during Kafka I/O:
     * 1. short transaction — load batch with pessimistic lock, then commit (lock released);
     * 2. no transaction   — send each message to Kafka;
     * 3. short transaction — bulk-update results: sent ones → SENT,
     *    failed ones — increment retry_count, mark as FAILED if maxRetries exhausted.
     * RU: Отправляет батч сообщений и помечает их как SENT.
     * Выполняется в трёх фазах, чтобы не удерживать DB-соединение во время Kafka I/O:
     * 1. короткая транзакция — загрузить батч с пессимистической блокировкой, закоммитить;
     * 2. без транзакции     — отправить каждое сообщение в Kafka;
     * 3. короткая транзакция — bulk-обновить результаты: успешные → SENT,
     *    неудачные — инкрементировать retry_count, при исчерпании попыток → FAILED.
     */
    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:1000}")
    public void publishPending() {
        List<OutboxMessage> batch = transactionTemplate.execute(
                status -> batchReader.loadPending(properties.getPublisher().getBatchSize())
        );

        if (batch == null || batch.isEmpty()) {
            return;
        }

        List<UUID> sentIds = new ArrayList<>();
        for (OutboxMessage message : batch) {
            if (sender.send(message)) {
                sentIds.add(message.getId());
            }
        }

        Instant now = Instant.now(clock);
        int maxRetries = properties.getPublisher().getMaxRetries();

        Set<UUID> sentSet = Set.copyOf(sentIds);
        List<UUID> failedIds = batch.stream()
                .map(OutboxMessage::getId)
                .filter(id -> !sentSet.contains(id))
                .toList();

        transactionTemplate.executeWithoutResult(status -> {
            if (!sentIds.isEmpty()) {
                repository.markSentByIds(sentIds, OutboxMessageStatus.SENT, now);
            }
            if (!failedIds.isEmpty()) {
                repository.incrementRetryCount(failedIds);
                log.warn("{} outbox message(s) failed to send; retry_count incremented", failedIds.size());
                int permanentlyFailed = repository.markFailedByIds(failedIds, OutboxMessageStatus.FAILED, maxRetries);
                if (permanentlyFailed > 0) {
                    log.error("{} outbox message(s) permanently failed after {} retries",
                              permanentlyFailed, maxRetries);
                }
            }
        });
    }
}