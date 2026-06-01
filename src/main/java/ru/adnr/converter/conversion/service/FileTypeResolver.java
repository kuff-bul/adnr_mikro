package ru.adnr.converter.conversion.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import ru.adnr.converter.conversion.model.FileType;

@Component
public class FileTypeResolver {

    private static final Map<String, FileType> FILE_TYPES_BY_EXTENSION = Map.of(
            "txt", FileType.TXT,
            "png", FileType.PNG,
            "jpg", FileType.JPG,
            "jpeg", FileType.JPG,
            "zip", FileType.ZIP
    );

    public Optional<FileType> resolve(String fileName) {
        int extensionSeparatorIndex = fileName.lastIndexOf('.');
        if (extensionSeparatorIndex < 0 || extensionSeparatorIndex == fileName.length() - 1) {
            return Optional.empty();
        }

        String extension = fileName.substring(extensionSeparatorIndex + 1).toLowerCase(Locale.ROOT);
        return Optional.ofNullable(FILE_TYPES_BY_EXTENSION.get(extension));
    }
}
