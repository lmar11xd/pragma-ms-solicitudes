package co.com.bancolombia.consumer.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicantPropertiesConfigTest {

    @Test
    void shouldSetAndGetBaseUrl() {
        // given
        ApplicantPropertiesConfig config = new ApplicantPropertiesConfig();
        String expectedUrl = "http://localhost:8080";

        // when
        config.setBaseUrl(expectedUrl);

        // then
        assertEquals(expectedUrl, config.getBaseUrl());
    }
}
