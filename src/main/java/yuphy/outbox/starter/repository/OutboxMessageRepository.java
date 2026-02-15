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

/** JPA repository for outbox messages. */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    /** Fetches a batch of messages with a pessimistic write lock. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from OutboxMessage m where m.status = :status order by m.createdAt")
    List<OutboxMessage> findBatchForUpdate(@Param("status") OutboxMessageStatus status, Pageable pageable);
}
