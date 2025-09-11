package co.com.bancolombia.sqs.sender.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SQSSenderPropertiesTest {

    @Test
    void shouldSetAndGetBaseUrl() {
        // given
        String expectedRegion = "http://localhost:8080";
        String expectedQueueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/MyQueue";
        String expectedEndpoint = "https://sqs.us-east-1.amazonaws.com";

        // when
        SQSSenderProperties config = new SQSSenderProperties(expectedRegion, expectedQueueUrl, expectedEndpoint);

        // then
        assertEquals(expectedRegion, config.region());
        assertEquals(expectedQueueUrl, config.queueUrl());
        assertEquals(expectedEndpoint, config.endpoint());
    }
}