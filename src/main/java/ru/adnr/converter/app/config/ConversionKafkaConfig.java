package ru.adnr.converter.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.adnr.converter.app.dto.FileConversionCompletedEvent;
import ru.adnr.converter.app.dto.FileConversionRequestedEvent;

@EnableKafka
@Configuration
@EnableConfigurationProperties(AppKafkaProperties.class)
public class ConversionKafkaConfig {

    @Bean
    public ConsumerFactory<String, FileConversionRequestedEvent> fileConversionRequestedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        JsonDeserializer<FileConversionRequestedEvent> valueDeserializer =
                new JsonDeserializer<>(FileConversionRequestedEvent.class, objectMapper, false);
        valueDeserializer.addTrustedPackages("ru.adnr.converter.app.dto");
        valueDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FileConversionRequestedEvent>
    fileConversionRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, FileConversionRequestedEvent> fileConversionRequestedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, FileConversionRequestedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(fileConversionRequestedConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ProducerFactory<String, FileConversionCompletedEvent> fileConversionCompletedProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        JsonSerializer<FileConversionCompletedEvent> valueSerializer = new JsonSerializer<>(objectMapper);
        valueSerializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
                properties,
                new StringSerializer(),
                valueSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, FileConversionCompletedEvent> fileConversionCompletedKafkaTemplate(
            ProducerFactory<String, FileConversionCompletedEvent> fileConversionCompletedProducerFactory
    ) {
        return new KafkaTemplate<>(fileConversionCompletedProducerFactory);
    }
}
