package ru.adnr.converter.conversion.service;

import java.util.Optional;
import ru.adnr.converter.conversion.model.FileType;

public interface FileTypeResolver {

    Optional<FileType> resolve(String fileName);
}
