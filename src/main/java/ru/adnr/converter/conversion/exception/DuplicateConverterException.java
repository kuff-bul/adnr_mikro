package ru.adnr.converter.conversion.exception;

import ru.adnr.converter.conversion.model.FileType;

public class DuplicateConverterException extends RuntimeException {

    public DuplicateConverterException(FileType fileType) {
        super("More than one converter registered for file type: " + fileType);
    }
}
