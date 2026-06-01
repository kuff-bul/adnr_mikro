package ru.adnr.converter.app.dto;

import ru.adnr.converter.conversion.model.FileType;

public record FileConversionCompletedEvent(
        String messageId,
        String correlationId,
        String sourceBucket,
        String sourceObjectKey,
        String pdfBucket,
        String pdfObjectKey,
        FileType fileType
) {
}
