package ru.adnr.converter.inbox.service;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.adnr.converter.inbox.entity.InboxMessage;
import ru.adnr.converter.inbox.entity.InboxStatus;
import ru.adnr.converter.inbox.repository.InboxMessageRepository;
import ru.adnr.converter.inbox.service.impl.InboxServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboxServiceTest {

    @Mock
    private InboxMessageRepository repository;

    @InjectMocks
    private InboxServiceImpl inboxService;

    @Test
    void registersMessageWhenItDoesNotExist() {
        when(repository.findByMessageId("msg-1")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(InboxMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean registered = inboxService.registerMessageIfNotExists("msg-1", "{\"value\":1}");

        ArgumentCaptor<InboxMessage> captor = ArgumentCaptor.forClass(InboxMessage.class);
        verify(repository).saveAndFlush(captor.capture());

        assertThat(registered).isTrue();
        assertThat(captor.getValue().getMessageId()).isEqualTo("msg-1");
        assertThat(captor.getValue().getPayload()).isEqualTo("{\"value\":1}");
        assertThat(captor.getValue().getStatus()).isEqualTo(InboxStatus.NEW);
    }

    @Test
    void doesNotRegisterExistingMessage() {
        when(repository.findByMessageId("msg-1")).thenReturn(Optional.of(new InboxMessage("msg-1", "{}")));

        boolean registered = inboxService.registerMessageIfNotExists("msg-1", "{}");

        assertThat(registered).isFalse();
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void handlesDuplicateMessageRaceCondition() {
        when(repository.findByMessageId("msg-1")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(InboxMessage.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        boolean registered = inboxService.registerMessageIfNotExists("msg-1", "{}");

        assertThat(registered).isFalse();
    }

    @Test
    void marksNewMessageAsProcessing() {
        InboxMessage message = new InboxMessage("msg-1", "{}");
        when(repository.findWithLockByMessageId("msg-1")).thenReturn(Optional.of(message));

        boolean marked = inboxService.markProcessing("msg-1");

        assertThat(marked).isTrue();
        assertThat(message.getStatus()).isEqualTo(InboxStatus.PROCESSING);
        assertThat(message.getErrorMessage()).isNull();
    }

    @Test
    void doesNotMarkProcessedMessageAsProcessingAgain() {
        InboxMessage message = new InboxMessage("msg-1", "{}");
        message.markProcessed();
        when(repository.findWithLockByMessageId("msg-1")).thenReturn(Optional.of(message));

        boolean marked = inboxService.markProcessing("msg-1");

        assertThat(marked).isFalse();
        assertThat(message.getStatus()).isEqualTo(InboxStatus.PROCESSED);
    }

    @Test
    void marksFailedButDoesNotOverrideProcessedMessage() {
        InboxMessage message = new InboxMessage("msg-1", "{}");
        message.markProcessed();
        when(repository.findWithLockByMessageId("msg-1")).thenReturn(Optional.of(message));

        inboxService.markFailed("msg-1", "boom");

        assertThat(message.getStatus()).isEqualTo(InboxStatus.PROCESSED);
        assertThat(message.getErrorMessage()).isNull();
    }

    @Test
    void checksAlreadyProcessed() {
        when(repository.existsByMessageIdAndStatus("msg-1", InboxStatus.PROCESSED)).thenReturn(true);

        assertThat(inboxService.isAlreadyProcessed("msg-1")).isTrue();
    }
}
