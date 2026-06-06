package ru.adnr.converter.app.dto;

public record FileConversionErrorEvent(
        String fileId,
        String errorMessage
) {
}
