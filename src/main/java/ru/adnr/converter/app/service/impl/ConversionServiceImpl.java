package ru.adnr.converter.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.adnr.converter.app.dto.FileConversionCompletedEvent;
import ru.adnr.converter.app.dto.FileConversionRequestedEvent;
import ru.adnr.converter.app.exception.ConversionProcessingException;
import ru.adnr.converter.app.kafka.ConversionEventProducer;
import ru.adnr.converter.app.service.ConversionService;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.converter.FileConverter;
import ru.adnr.converter.conversion.exception.ConversionException;
import ru.adnr.converter.conversion.model.FileType;
import ru.adnr.converter.conversion.registry.ConverterRegistry;
import ru.adnr.converter.conversion.service.FileTypeResolver;
import ru.adnr.converter.inbox.service.InboxService;
import ru.adnr.converter.storage.config.MinioProperties;
import ru.adnr.converter.storage.service.StorageService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionServiceImpl implements ConversionService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final InboxService inboxService;
    private final StorageService storageService;
    private final ConverterRegistry converterRegistry;
    private final FileTypeResolver fileTypeResolver;
    private final ConversionEventProducer conversionEventProducer;
    private final ObjectMapper objectMapper;
    private final MinioProperties minioProperties;

    @Override
    public void process(FileConversionRequestedEvent event) {
        if (inboxService.isAlreadyProcessed(event.messageId())) {
            log.info("Skipping already processed message: {}", event.messageId());
            return;
        }

        boolean registered = inboxService.registerMessageIfNotExists(event.messageId(), serialize(event));
        if (!registered) {
            log.info("Skipping already registered message: {}", event.messageId());
            return;
        }

        try {
            if (!inboxService.markProcessing(event.messageId())) {
                log.info("Skipping message because it is already being processed: {}", event.messageId());
                return;
            }

            processRegisteredMessage(event);
            inboxService.markProcessed(event.messageId());
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            String errorMessage = buildErrorMessage(ex);
            inboxService.markFailed(event.messageId(), errorMessage);
            log.error("Failed to process conversion message. messageId={}, reason={}",
                    event.messageId(), errorMessage, ex);
            throw new ConversionProcessingException(
                    "Failed to process conversion message: " + event.messageId(), ex);
        }
    }

    private void processRegisteredMessage(FileConversionRequestedEvent event)
            throws IOException, ExecutionException, InterruptedException {
        String sourceBucket = resolveBucket(event.bucket());
        String originalFileName = resolveOriginalFileName(event);
        FileType fileType = resolveFileType(event, originalFileName);
        FileConverter converter = converterRegistry.getConverter(fileType);

        byte[] pdfContent;
        try (InputStream inputStream = storageService.download(sourceBucket, event.objectKey())) {
            ConversionContext context = new ConversionContext(originalFileName, event.messageId(), fileType);
            pdfContent = converter.convertToPdf(inputStream, context);
        }

        String pdfBucket = sourceBucket;
        String pdfObjectKey = buildPdfObjectKey(event, originalFileName);
        storageService.upload(pdfBucket, pdfObjectKey, pdfContent, PDF_CONTENT_TYPE);

        FileConversionCompletedEvent completedEvent = new FileConversionCompletedEvent(
                event.messageId(),
                event.messageId(),
                sourceBucket,
                event.objectKey(),
                pdfBucket,
                pdfObjectKey,
                fileType
        );

        conversionEventProducer.sendCompleted(completedEvent);
    }

    private String resolveBucket(String bucket) {
        if (StringUtils.hasText(bucket)) {
            return bucket;
        }
        return minioProperties.bucket();
    }

    private FileType resolveFileType(FileConversionRequestedEvent event, String originalFileName) {
        if (event.fileType() != null) {
            return event.fileType();
        }

        return fileTypeResolver.resolve(originalFileName)
                .orElseThrow(() -> new ConversionException("Unable to resolve file type for: " + originalFileName));
    }

    private String resolveOriginalFileName(FileConversionRequestedEvent event) {
        if (StringUtils.hasText(event.originalFileName())) {
            return event.originalFileName();
        }

        int separatorIndex = event.objectKey().lastIndexOf('/');
        return separatorIndex >= 0 ? event.objectKey().substring(separatorIndex + 1) : event.objectKey();
    }

    private String buildPdfObjectKey(FileConversionRequestedEvent event, String originalFileName) {
        String baseName = originalFileName;
        int extensionIndex = baseName.lastIndexOf('.');
        if (extensionIndex > 0) {
            baseName = baseName.substring(0, extensionIndex);
        }

        String normalizedBaseName = baseName
                .replace('\\', '/')
                .replaceAll("[^a-zA-Z0-9._-]+", "_")
                .toLowerCase(Locale.ROOT);

        return "converted/%s/%s.pdf".formatted(event.messageId(), normalizedBaseName);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ConversionProcessingException("Failed to serialize conversion event", ex);
        }
    }

    private String buildErrorMessage(Exception ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();
        if (!StringUtils.hasText(message)) {
            message = rootCause.getClass().getSimpleName();
        }
        return message;
    }
}
