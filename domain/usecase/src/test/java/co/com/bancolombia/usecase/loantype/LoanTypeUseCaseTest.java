package co.com.bancolombia.usecase.loantype;

import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class LoanTypeUseCaseTest {
    @Mock
    private LoanTypeRepository loanTypeRepository;

    private LoanTypeUseCase loanTypeUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanTypeUseCase = new LoanTypeUseCase(loanTypeRepository);
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenLoanTypeExists() {
        // given
        String code = "PERSONAL";
        when(loanTypeRepository.existsByCode(code)).thenReturn(Mono.just(true));

        // when - then
        StepVerifier.create(loanTypeUseCase.existsByCode(code))
                .expectNext(true)
                .verifyComplete();

        verify(loanTypeRepository, times(1)).existsByCode(code);
    }

    @Test
    void existsByCode_ShouldReturnFalse_WhenLoanTypeDoesNotExist() {
        // given
        String code = "MORTGAGE";
        when(loanTypeRepository.existsByCode(code)).thenReturn(Mono.just(false));

        // when - then
        StepVerifier.create(loanTypeUseCase.existsByCode(code))
                .expectNext(false)
                .verifyComplete();

        verify(loanTypeRepository, times(1)).existsByCode(code);
    }

    @Test
    void existsByCode_ShouldPropagateError_WhenRepositoryFails() {
        // given
        String code = "AUTO";
        RuntimeException exception = new RuntimeException("DB error");
        when(loanTypeRepository.existsByCode(code)).thenReturn(Mono.error(exception));

        // when - then
        StepVerifier.create(loanTypeUseCase.existsByCode(code))
                .expectErrorMatches(err -> err instanceof RuntimeException
                        && err.getMessage().equals("DB error"))
                .verify();

        verify(loanTypeRepository, times(1)).existsByCode(code);
    }
}
