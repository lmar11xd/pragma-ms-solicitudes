package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.config.ApplicantPropertiesConfig;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.exception.ExternalServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class ApplicantClientTest {

    private MockWebServer mockWebServer;
    private ApplicantClient applicantClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ApplicantPropertiesConfig properties = new ApplicantPropertiesConfig();
        properties.setBaseUrl(mockWebServer.url("/api/applicants").toString());

        applicantClient = new ApplicantClient(WebClient.builder(), properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnApplicantWhenFound() {
        String body = """
            {
              "id": "123",
              "documentNumber": "100",
              "email": "test@mail.com"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(applicantClient.findApplicantByDocumentNumber("100", "validToken"))
                .expectNextMatches(applicant ->
                        applicant.getId().equals("123") &&
                                applicant.getEmail().equals("test@mail.com"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(applicantClient.findApplicantByDocumentNumber("200", "validToken"))
                .verifyComplete();
    }

    @Test
    void shouldThrowUnauthorizedException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        StepVerifier.create(applicantClient.findApplicantByDocumentNumber("300", "invalidToken"))
                .expectErrorMatches(ex -> ex instanceof ExternalServiceException &&
                        ((ExternalServiceException) ex).getMessage()
                                .contains(ErrorCode.UNAUTHORIZED.getDefaultMessage()))
                .verify();
    }

    @Test
    void shouldThrowForbiddenException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        StepVerifier.create(applicantClient.findApplicantByDocumentNumber("400", "token"))
                .expectErrorMatches(ex -> ex instanceof ExternalServiceException &&
                        ((ExternalServiceException) ex).getMessage()
                                .contains(ErrorCode.FORBIDDEN.getDefaultMessage()))
                .verify();
    }

    @Test
    void shouldThrowServerErrorException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.create(applicantClient.findApplicantByDocumentNumber("500", "token"))
                .expectErrorMatches(ex -> ex instanceof ExternalServiceException &&
                        ((ExternalServiceException) ex).getMessage()
                                .contains(ErrorCode.APPLICANT_SERVICE_FAILED.getDefaultMessage()))
                .verify();
    }
}