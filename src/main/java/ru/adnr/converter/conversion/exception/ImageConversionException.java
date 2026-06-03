package ru.adnr.converter.conversion.exception;

public class ImageConversionException extends ConversionException {

    public ImageConversionException(String fileName, Throwable cause) {
        super("Failed to convert image file to PDF: " + fileName, cause);
    }
}
