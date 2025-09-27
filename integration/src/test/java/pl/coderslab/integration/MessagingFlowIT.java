package pl.coderslab.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.gatherer.DataGathererApplication;
import pl.coderslab.gatherer.messaging.CurrencyChangePublisher;
import pl.coderslab.provider.DataProviderApplication;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@SpringBootTest(classes = DataProviderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class MessagingFlowIT {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    private static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management");

    static {
        POSTGRES.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    static void configureDataProviderProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration/dataprovider");
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private ConfigurableApplicationContext gathererContext;
    private final Map<String, String> overriddenSystemProperties = new HashMap<>();

    @BeforeEach
    void setUp() {
        applicationContext.getBeanProvider(Flyway.class).ifAvailable(Flyway::migrate);
        currencyRateRepository.deleteAll();
        overrideSystemProperty("DATAGATHERER_DB_URL", POSTGRES.getJdbcUrl());
        overrideSystemProperty("DATAGATHERER_DB_USERNAME", POSTGRES.getUsername());
        overrideSystemProperty("DATAGATHERER_DB_PASSWORD", POSTGRES.getPassword());
        overrideSystemProperty("DATAGATHERER_DB_SCHEMA", "public");
        overrideSystemProperty("spring.rabbitmq.host", RABBIT.getHost());
        overrideSystemProperty("spring.rabbitmq.port", String.valueOf(RABBIT.getAmqpPort()));
        overrideSystemProperty("spring.rabbitmq.username", RABBIT.getAdminUsername());
        overrideSystemProperty("spring.rabbitmq.password", RABBIT.getAdminPassword());

        gathererContext = new SpringApplicationBuilder(DataGathererApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.jpa.hibernate.ddl-auto=none",
                        "spring.flyway.enabled=true",
                        "spring.flyway.locations=classpath:db/migration/datagatherer",
                        "spring.flyway.schemas=public")
                .run();
    }

    @AfterEach
    void tearDown() {
        if (gathererContext != null) {
            gathererContext.close();
        }
        currencyRateRepository.deleteAll();
        restoreSystemProperties();
    }

    @AfterAll
    static void shutdownContainers() {
        RABBIT.stop();
        POSTGRES.stop();
    }

    @Test
    void shouldPersistRateEmittedByDataGatherer() {
        CurrencyChangePublisher publisher = gathererContext.getBean(CurrencyChangePublisher.class);
        String currencyCode = "TST";
        CurrencyRateChangeMessage message = new CurrencyRateChangeMessage(
                "USD",
                currencyCode,
                new BigDecimal("4.123400"),
                new BigDecimal("1.25"),
                Instant.now()
        );

        publisher.publish(message);

        CurrencyRate persisted = awaitPersistedRate(currencyCode);

        Assertions.assertEquals(currencyCode, persisted.getCurrencyCode());
        Assertions.assertEquals(0, new BigDecimal("4.123400").compareTo(persisted.getRate()));
    }

    private CurrencyRate awaitPersistedRate(String currencyCode) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline)) {
            var page = currencyRateRepository.findByCurrencyCode(
                    currencyCode,
                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"))
            );
            if (!page.isEmpty()) {
                return page.getContent().get(0);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for rate persistence", e);
            }
        }
        throw new IllegalStateException("Timed out waiting for rate persistence");
    }

    private void overrideSystemProperty(String key, String value) {
        overriddenSystemProperties.put(key, System.getProperty(key));
        System.setProperty(key, value);
    }

    private void restoreSystemProperties() {
        for (Map.Entry<String, String> entry : overriddenSystemProperties.entrySet()) {
            if (entry.getValue() == null) {
                System.clearProperty(entry.getKey());
            } else {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
        overriddenSystemProperties.clear();
    }
}
