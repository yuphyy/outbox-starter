package yuphy.outbox.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/** Smoke test to verify auto-configuration loads. */
@SpringBootTest(classes = OutboxStarterApplicationTests.TestApp.class)
class OutboxStarterApplicationTests {

    @Test
    /** Starts the Spring context. */
    void contextLoads() {
    }

    /** Minimal Spring Boot app for tests. */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @AutoConfigurationPackage(basePackages = "yuphy.outbox.testapp")
    static class TestApp {
    }

}
