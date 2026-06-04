package ru.adnr.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DocumentConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentConverterApplication.class, args);
    }
}
