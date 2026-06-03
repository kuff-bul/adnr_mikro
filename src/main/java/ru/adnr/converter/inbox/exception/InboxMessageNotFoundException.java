package ru.adnr.converter.inbox.exception;

public class InboxMessageNotFoundException extends RuntimeException {

    public InboxMessageNotFoundException(String messageId) {
        super("Inbox message not found: " + messageId);
    }
}
