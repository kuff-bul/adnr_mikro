package ru.adnr.converter.conversion.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ru.adnr.converter.conversion.converter.FileConverter;
import ru.adnr.converter.conversion.exception.ConverterNotFoundException;
import ru.adnr.converter.conversion.exception.DuplicateConverterException;
import ru.adnr.converter.conversion.model.FileType;

@Component
public class ConverterRegistry {

    private final Map<FileType, FileConverter> convertersByType;

    public ConverterRegistry(List<FileConverter> converters) {
        this.convertersByType = buildConvertersMap(converters);
    }

    public FileConverter getConverter(FileType fileType) {
        FileConverter converter = convertersByType.get(fileType);
        if (converter == null) {
            throw new ConverterNotFoundException(fileType);
        }
        return converter;
    }

    private Map<FileType, FileConverter> buildConvertersMap(List<FileConverter> converters) {
        Map<FileType, FileConverter> result = new EnumMap<>(FileType.class);

        for (FileType fileType : FileType.values()) {
            List<FileConverter> supportedConverters = converters.stream()
                    .filter(converter -> converter.supports(fileType))
                    .toList();

            if (supportedConverters.size() > 1) {
                throw new DuplicateConverterException(fileType);
            }

            if (supportedConverters.size() == 1) {
                result.put(fileType, supportedConverters.getFirst());
            }
        }

        return Map.copyOf(result);
    }
}
