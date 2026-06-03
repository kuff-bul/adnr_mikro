package ru.adnr.converter.app.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.adnr.converter.app.dto.FileConversionRequestedEvent;
import ru.adnr.converter.app.exception.ConversionProcessingException;
import ru.adnr.converter.conversion.model.FileType;

class FileConversionRequestedEventValidatorTest {

    private final FileConversionRequestedEventValidator validator = new FileConversionRequestedEventValidator();

    @Test
    void validatesCorrectEvent() {
        FileConversionRequestedEvent event = new FileConversionRequestedEvent(
                "message-id",
                "bucket",
                "path/file.txt",
                FileType.TXT,
                "file.txt"
        );

        assertThatCode(() -> validator.validate(event)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullEvent() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(ConversionProcessingException.class)
                .hasMessage("Conversion event must not be null");
    }

    @Test
    void rejectsBlankMessageId() {
        FileConversionRequestedEvent event = new FileConversionRequestedEvent(
                " ",
                "bucket",
                "path/file.txt",
                FileType.TXT,
                "file.txt"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ConversionProcessingException.class)
                .hasMessage("Conversion event messageId must not be blank");
    }

    @Test
    void rejectsBlankObjectKey() {
        FileConversionRequestedEvent event = new FileConversionRequestedEvent(
                "message-id",
                "bucket",
                "",
                FileType.TXT,
                "file.txt"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ConversionProcessingException.class)
                .hasMessage("Conversion event objectKey must not be blank");
    }
}
