package ru.adnr.converter.inbox.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.adnr.converter.inbox.entity.InboxMessage;
import ru.adnr.converter.inbox.entity.InboxStatus;
import ru.adnr.converter.inbox.repository.InboxMessageRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestEntityManager
@Import(InboxService.class)
class InboxServicePostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("document_converter_test")
            .withUsername("document_converter")
            .withPassword("document_converter");

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxMessageRepository repository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yml");
        registry.add("spring.test.database.replace", () -> "none");
    }

    @Test
    void registersSameMessageIdOnlyOnce() {
        boolean firstRegistration = inboxService.registerMessageIfNotExists("same-message-id", "{\"attempt\":1}");
        boolean secondRegistration = inboxService.registerMessageIfNotExists("same-message-id", "{\"attempt\":2}");

        assertThat(firstRegistration).isTrue();
        assertThat(secondRegistration).isFalse();
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findByMessageId("same-message-id"))
                .get()
                .extracting(InboxMessage::getPayload)
                .isEqualTo("{\"attempt\":1}");
    }

    @Test
    void processesSameMessageIdOnlyOnce() {
        inboxService.registerMessageIfNotExists("process-once-id", "{}");

        boolean firstProcessing = inboxService.markProcessing("process-once-id");
        inboxService.markProcessed("process-once-id");
        boolean secondProcessing = inboxService.markProcessing("process-once-id");

        assertThat(firstProcessing).isTrue();
        assertThat(secondProcessing).isFalse();
        assertThat(inboxService.isAlreadyProcessed("process-once-id")).isTrue();
        assertThat(repository.findByMessageId("process-once-id"))
                .get()
                .extracting(InboxMessage::getStatus)
                .isEqualTo(InboxStatus.PROCESSED);
    }
}
