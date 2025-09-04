package co.com.bancolombia.consumer.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WebClientConfig.class)
class WebClientConfigTest {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    void shouldLoadWebClientBuilderFromContext() {
        assertNotNull(webClientBuilder);
        WebClient client = webClientBuilder.baseUrl("http://localhost").build();
        assertNotNull(client);
    }
}