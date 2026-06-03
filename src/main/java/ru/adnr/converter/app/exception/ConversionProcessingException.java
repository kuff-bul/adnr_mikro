package ru.adnr.converter.app.exception;

public class ConversionProcessingException extends RuntimeException {

    public ConversionProcessingException(String message) {
        super(message);
    }

    public ConversionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
