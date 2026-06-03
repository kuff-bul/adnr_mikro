package ru.adnr.converter.conversion.context;

import ru.adnr.converter.conversion.model.FileType;

public record ConversionContext(
        String originalFileName,
        String correlationId,
        FileType fileType
) {
}
