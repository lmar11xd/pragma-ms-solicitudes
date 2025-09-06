package co.com.bancolombia.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

class RouterRestTest {

    private WebTestClient webTestClient;
    private Handler handler;

    @BeforeEach
    void setUp() {
        handler = Mockito.mock(Handler.class);

        // Simular comportamiento del handler.create
        Mockito.when(handler.create(Mockito.any()))
                .thenReturn(ServerResponse.created(null).build());

        // Simular comportamiento del handler.getPendingApplications
        Mockito.when(handler.listApplications(Mockito.any()))
                .thenReturn(ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("[]"));

        RouterRest routerRest = new RouterRest();
        webTestClient = WebTestClient.bindToRouterFunction(routerRest.routerFunction(handler)).build();
    }

    @Test
    void shouldRoutePostToCreate() {
        webTestClient.post()
                .uri("/api/v1/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"documentNumber\":\"123\",\"amount\":1000,\"termMonths\":12,\"loanTypeCode\":\"CODE1\"}")
                .exchange()
                .expectStatus().isCreated();
    }
}