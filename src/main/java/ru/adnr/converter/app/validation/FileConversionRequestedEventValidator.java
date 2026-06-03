package ru.adnr.converter.app.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.adnr.converter.app.dto.FileConversionRequestedEvent;
import ru.adnr.converter.app.exception.ConversionProcessingException;

@Component
public class FileConversionRequestedEventValidator {

    public void validate(FileConversionRequestedEvent event) {
        if (event == null) {
            throw new ConversionProcessingException("Conversion event must not be null");
        }
        if (!StringUtils.hasText(event.messageId())) {
            throw new ConversionProcessingException("Conversion event messageId must not be blank");
        }
        if (!StringUtils.hasText(event.objectKey())) {
            throw new ConversionProcessingException("Conversion event objectKey must not be blank");
        }
    }
}
