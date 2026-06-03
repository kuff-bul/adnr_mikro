package ru.adnr.converter.app.service;

import ru.adnr.converter.app.dto.FileConversionRequestedEvent;

public interface ConversionService {

    void process(FileConversionRequestedEvent event);
}
