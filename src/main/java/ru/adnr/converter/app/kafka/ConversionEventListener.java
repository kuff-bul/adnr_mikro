package ru.adnr.converter.app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.adnr.converter.app.dto.FileConversionRequestedEvent;
import ru.adnr.converter.app.exception.ConversionProcessingException;
import ru.adnr.converter.app.service.ConversionService;
import ru.adnr.converter.app.validation.FileConversionRequestedEventValidator;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversionEventListener {

    private final FileConversionRequestedEventValidator eventValidator;
    private final ConversionService conversionService;

    @KafkaListener(
            topics = "${app.kafka.input-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "fileConversionRequestedKafkaListenerContainerFactory"
    )
    public void listen(FileConversionRequestedEvent event, Acknowledgment acknowledgment) {
        try {
            eventValidator.validate(event);
            conversionService.process(event);
            acknowledgment.acknowledge();
        } catch (ConversionProcessingException ex) {
            log.error("Conversion request processing failed. Offset will be acknowledged because failure "
                    + "details are stored in inbox", ex);
            acknowledgment.acknowledge();
        }
    }
}
