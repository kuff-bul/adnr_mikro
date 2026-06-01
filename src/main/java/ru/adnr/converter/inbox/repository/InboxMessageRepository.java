package ru.adnr.converter.inbox.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.adnr.converter.inbox.entity.InboxMessage;
import ru.adnr.converter.inbox.entity.InboxStatus;

import jakarta.persistence.LockModeType;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, Long> {

    boolean existsByMessageIdAndStatus(String messageId, InboxStatus status);

    Optional<InboxMessage> findByMessageId(String messageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InboxMessage> findWithLockByMessageId(String messageId);
}
