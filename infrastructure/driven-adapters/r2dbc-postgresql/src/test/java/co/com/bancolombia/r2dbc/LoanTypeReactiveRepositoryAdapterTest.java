package co.com.bancolombia.r2dbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanTypeReactiveRepositoryAdapterTest {

    @Mock
    private LoanTypeReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    private LoanTypeReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new LoanTypeReactiveRepositoryAdapter(repository, mapper);
    }

    @Test
    void existsByCodeShouldReturnTrueWhenLoanTypeExists() {
        // Arrange
        String code = "HOME";
        when(repository.existsByCode(anyString())).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = adapter.existsByCode(code);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByCodeShouldReturnFalseWhenLoanTypeDoesNotExist() {
        // Arrange
        String code = "INVALID";
        when(repository.existsByCode(anyString())).thenReturn(Mono.just(false));

        // Act
        Mono<Boolean> result = adapter.existsByCode(code);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}
