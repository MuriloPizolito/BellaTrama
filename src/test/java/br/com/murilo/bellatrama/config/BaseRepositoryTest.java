package br.com.murilo.bellatrama.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

    @ServiceConnection
    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18");

    static {
        POSTGRES.start();
    }

    @PersistenceContext
    protected EntityManager entityManager;

}
