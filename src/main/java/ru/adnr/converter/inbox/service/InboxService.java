package ru.adnr.converter.inbox.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.adnr.converter.inbox.entity.InboxMessage;
import ru.adnr.converter.inbox.entity.InboxStatus;
import ru.adnr.converter.inbox.exception.InboxMessageNotFoundException;
import ru.adnr.converter.inbox.repository.InboxMessageRepository;

@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxMessageRepository inboxMessageRepository;

    @Transactional
    public boolean registerMessageIfNotExists(String messageId, String payload) {
        if (inboxMessageRepository.findByMessageId(messageId).isPresent()) {
            return false;
        }

        try {
            inboxMessageRepository.saveAndFlush(new InboxMessage(messageId, payload));
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    @Transactional
    public boolean markProcessing(String messageId) {
        InboxMessage message = inboxMessageRepository.findWithLockByMessageId(messageId)
                .orElseThrow(() -> new InboxMessageNotFoundException(messageId));

        if (message.getStatus() == InboxStatus.PROCESSED || message.getStatus() == InboxStatus.PROCESSING) {
            return false;
        }

        message.markProcessing();
        return true;
    }

    @Transactional
    public void markProcessed(String messageId) {
        InboxMessage message = inboxMessageRepository.findWithLockByMessageId(messageId)
                .orElseThrow(() -> new InboxMessageNotFoundException(messageId));

        message.markProcessed();
    }

    @Transactional
    public void markFailed(String messageId, String errorMessage) {
        InboxMessage message = inboxMessageRepository.findWithLockByMessageId(messageId)
                .orElseThrow(() -> new InboxMessageNotFoundException(messageId));

        if (message.getStatus() == InboxStatus.PROCESSED) {
            return;
        }

        message.markFailed(errorMessage);
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String messageId) {
        return inboxMessageRepository.existsByMessageIdAndStatus(messageId, InboxStatus.PROCESSED);
    }
}
