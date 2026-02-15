package yuphy.outbox.starter.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import yuphy.outbox.starter.model.OutboxMessage;
import yuphy.outbox.starter.model.OutboxMessageStatus;

/**
 * EN: JPA repository for outbox messages.
 * RU: JPA-репозиторий для сообщений outbox.
 */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    /**
     * EN: Fetches a batch of messages with a pessimistic write lock.
     * RU: Получает батч сообщений с пессимистической блокировкой записи.
     *
     * @param status EN: message status to filter by. RU: статус для фильтрации.
     * @param pageable EN: batch size and paging info. RU: размер батча и страница.
     * @return EN: list of messages. RU: список сообщений.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from OutboxMessage m where m.status = :status order by m.createdAt")
    List<OutboxMessage> findBatchForUpdate(@Param("status") OutboxMessageStatus status, Pageable pageable);
}
