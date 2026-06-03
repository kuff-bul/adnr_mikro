package ru.adnr.converter.inbox.service;

public interface InboxService {

    boolean registerMessageIfNotExists(String messageId, String payload);

    boolean markProcessing(String messageId);

    void markProcessed(String messageId);

    void markFailed(String messageId, String errorMessage);

    boolean isAlreadyProcessed(String messageId);
}
