package ru.adnr.converter.conversion.exception;

import ru.adnr.converter.conversion.model.FileType;

public class ConverterNotFoundException extends RuntimeException {

    public ConverterNotFoundException(FileType fileType) {
        super("Converter not found for file type: " + fileType);
    }
}
