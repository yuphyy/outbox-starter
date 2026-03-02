package yuphy.outbox.starter.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.model.OutboxMessageStatus;

/**
 * EN: JPA repository for outbox messages.
 * RU: JPA-репозиторий для сообщений outbox.
 */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    /**
     * EN: Fetches a batch of pending messages with a pessimistic write lock.
     * The SKIP_LOCKED hint (-2) makes competing instances skip already-locked rows
     * instead of blocking, which is essential for multi-instance deployments.
     * Requires PostgreSQL 9.5+ or MySQL 8.0+.
     * RU: Получает батч ожидающих сообщений с пессимистической блокировкой.
     * Хинт SKIP_LOCKED (-2) заставляет конкурирующие инстансы пропускать заблокированные строки
     * вместо ожидания — необходимо для мульти-инстансового деплоя.
     * Требует PostgreSQL 9.5+ или MySQL 8.0+.
     *
     * @param status EN: message status to filter by. RU: статус для фильтрации.
     * @param pageable EN: batch size and paging info. RU: размер батча и страница.
     * @return EN: list of messages. RU: список сообщений.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select m from OutboxMessage m where m.status = :status order by m.createdAt")
    List<OutboxMessage> findBatchForUpdate(@Param("status") OutboxMessageStatus status, Pageable pageable);

    /**
     * EN: Marks a set of messages as sent in a single bulk update.
     * RU: Помечает набор сообщений как SENT одним bulk-запросом.
     *
     * @param ids EN: message ids to update. RU: идентификаторы сообщений для обновления.
     * @param newStatus EN: new status. RU: новый статус.
     * @param sentAt EN: sent timestamp. RU: время отправки.
     */
    @Modifying(clearAutomatically = true)
    @Query("update OutboxMessage m set m.status = :newStatus, m.sentAt = :sentAt where m.id in :ids")
    void markSentByIds(@Param("ids") List<UUID> ids,
                       @Param("newStatus") OutboxMessageStatus newStatus,
                       @Param("sentAt") Instant sentAt);

    /**
     * EN: Increments retry count for the given messages.
     * RU: Увеличивает счётчик попыток для указанных сообщений.
     *
     * @param ids EN: message ids. RU: идентификаторы сообщений.
     */
    @Modifying(clearAutomatically = true)
    @Query("update OutboxMessage m set m.retryCount = m.retryCount + 1 where m.id in :ids")
    void incrementRetryCount(@Param("ids") List<UUID> ids);

    /**
     * EN: Marks messages as permanently failed when their retry count meets or exceeds the threshold.
     * RU: Помечает сообщения как окончательно неудавшиеся, если счётчик попыток достиг порога.
     *
     * @param ids EN: candidate message ids. RU: идентификаторы сообщений-кандидатов.
     * @param failedStatus EN: failed status value. RU: статус ошибки.
     * @param maxRetries EN: max retry threshold. RU: максимальное число попыток.
     */
    @Modifying(clearAutomatically = true)
    @Query("update OutboxMessage m set m.status = :failedStatus " +
           "where m.id in :ids and m.retryCount >= :maxRetries")
    int markFailedByIds(@Param("ids") List<UUID> ids,
                        @Param("failedStatus") OutboxMessageStatus failedStatus,
                        @Param("maxRetries") int maxRetries);
}
