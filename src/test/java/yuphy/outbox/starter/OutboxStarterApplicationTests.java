package yuphy.outbox.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OutboxStarterApplicationTests.TestApp.class)
class OutboxStarterApplicationTests {

    @Test
    void contextLoads() {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @AutoConfigurationPackage(basePackages = "yuphy.outbox.testapp")
    static class TestApp {
    }

}
