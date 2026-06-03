package ru.adnr.converter.conversion.exception;

public class TxtConversionException extends ConversionException {

    public TxtConversionException(String fileName, Throwable cause) {
        super("Failed to convert TXT file to PDF: " + fileName, cause);
    }
}
