package ru.adnr.converter.conversion.registry;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.adnr.converter.conversion.context.ConversionContext;
import ru.adnr.converter.conversion.converter.FileConverter;
import ru.adnr.converter.conversion.exception.ConverterNotFoundException;
import ru.adnr.converter.conversion.exception.DuplicateConverterException;
import ru.adnr.converter.conversion.model.FileType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterRegistryTest {

    @Test
    void returnsConverterForSupportedType() {
        FileConverter txtConverter = new StubConverter(FileType.TXT);
        ConverterRegistry registry = new ConverterRegistry(List.of(txtConverter));

        assertThat(registry.getConverter(FileType.TXT)).isSameAs(txtConverter);
    }

    @Test
    void throwsWhenConverterNotFound() {
        ConverterRegistry registry = new ConverterRegistry(List.of(new StubConverter(FileType.PNG)));

        assertThatThrownBy(() -> registry.getConverter(FileType.TXT))
                .isInstanceOf(ConverterNotFoundException.class)
                .hasMessageContaining("TXT");
    }

    @Test
    void throwsWhenMoreThanOneConverterSupportsType() {
        FileConverter first = new StubConverter(FileType.TXT);
        FileConverter second = new StubConverter(FileType.TXT);

        assertThatThrownBy(() -> new ConverterRegistry(List.of(first, second)))
                .isInstanceOf(DuplicateConverterException.class)
                .hasMessageContaining("TXT");
    }

    private record StubConverter(FileType supportedType) implements FileConverter {

        @Override
        public boolean supports(FileType fileType) {
            return fileType == supportedType;
        }

        @Override
        public byte[] convertToPdf(InputStream inputStream, ConversionContext context) {
            return new byte[0];
        }
    }
}
