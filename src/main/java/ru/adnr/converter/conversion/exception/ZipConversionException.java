package ru.adnr.converter.conversion.exception;

public class ZipConversionException extends ConversionException {

    public ZipConversionException(String fileName, Throwable cause) {
        super("Failed to convert ZIP file to PDF: " + fileName, cause);
    }

    public ZipConversionException(String message) {
        super(message);
    }
}
