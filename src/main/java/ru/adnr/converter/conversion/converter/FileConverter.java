package ru.adnr.converter.conversion.converter;

import java.io.InputStream;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.model.FileType;

public interface FileConverter {

    boolean supports(FileType fileType);

    byte[] convertToPdf(InputStream inputStream, ConversionContext context);
}
