package ru.adnr.converter.app.kafka;

import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.adnr.converter.app.config.AppKafkaProperties;
import ru.adnr.converter.app.dto.FileConversionErrorEvent;

@Component
public class ConversionErrorEventProducer {

    private final KafkaTemplate<String, FileConversionErrorEvent> kafkaTemplate;
    private final AppKafkaProperties kafkaProperties;

    public ConversionErrorEventProducer(
            @Qualifier("fileConversionErrorKafkaTemplate")
            KafkaTemplate<String, FileConversionErrorEvent> kafkaTemplate,
            AppKafkaProperties kafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public void sendError(FileConversionErrorEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(kafkaProperties.errorTopic(), event.fileId(), event).get();
    }
}
