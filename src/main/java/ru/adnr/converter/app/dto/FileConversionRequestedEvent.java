package ru.adnr.converter.app.dto;

import ru.adnr.converter.conversion.model.FileType;

public record FileConversionRequestedEvent(
        String messageId,
        String bucket,
        String objectKey,
        FileType fileType,
        String originalFileName
) {
}
