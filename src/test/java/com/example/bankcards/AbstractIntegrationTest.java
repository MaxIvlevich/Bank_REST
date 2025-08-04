package com.example.bankcards;

import com.example.bankcards.config.DataInitializer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests.
 * Manages the lifecycle of a PostgreSQL Docker container using Testcontainers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class AbstractIntegrationTest {

    @MockitoBean
    private DataInitializer dataInitializer;
    /**
     * Defines the PostgreSQL container.
     * The image 'postgres:16-alpine' is lightweight.
     * The container will be started once for all tests that extend this class.
     */
    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16-alpine");

    /**
     * Dynamically sets the datasource properties for the Spring context
     * based on the running Testcontainer.
     */
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
}
