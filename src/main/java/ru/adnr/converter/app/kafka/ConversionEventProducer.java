package ru.adnr.converter.app.kafka;

import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.adnr.converter.app.config.AppKafkaProperties;
import ru.adnr.converter.app.dto.FileConversionCompletedEvent;

@Component
public class ConversionEventProducer {

    private final KafkaTemplate<String, FileConversionCompletedEvent> kafkaTemplate;
    private final AppKafkaProperties kafkaProperties;

    public ConversionEventProducer(
            @Qualifier("fileConversionCompletedKafkaTemplate")
            KafkaTemplate<String, FileConversionCompletedEvent> kafkaTemplate,
            AppKafkaProperties kafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public void sendCompleted(FileConversionCompletedEvent event) throws ExecutionException, InterruptedException {
        kafkaTemplate.send(kafkaProperties.outputTopic(), event.messageId(), event).get();
    }
}
