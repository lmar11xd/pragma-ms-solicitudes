package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LoanApplicationUseCaseTest {
    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private LoanApplication buildValidLoanApplication() {
        return LoanApplication.builder()
                .documentNumber("12345678")
                .amount(BigDecimal.valueOf(1000))
                .termMonths(12)
                .loanTypeCode("CODE01")
                .build();
    }

    @Test
    void shouldReturnErrorWhenDocumentNumberIsBlank() {
        LoanApplication invalid = buildValidLoanApplication();
        invalid.setDocumentNumber("");

        StepVerifier.create(loanApplicationUseCase.create(invalid))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.REQUERID_DOCUMENTNUMBER)
                .verify();

        verifyNoInteractions(loanTypeRepository, loanApplicationRepository);
    }

    @Test
    void shouldReturnErrorWhenAmountIsNullOrZeroOrNegative() {
        LoanApplication invalid1 = buildValidLoanApplication();
        invalid1.setAmount(null);

        StepVerifier.create(loanApplicationUseCase.create(invalid1))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_AMOUNT)
                .verify();

        LoanApplication invalid2 = buildValidLoanApplication();
        invalid2.setAmount(BigDecimal.ZERO);

        StepVerifier.create(loanApplicationUseCase.create(invalid2))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_AMOUNT)
                .verify();

        LoanApplication invalid3 = buildValidLoanApplication();
        invalid3.setAmount(BigDecimal.valueOf(-50));

        StepVerifier.create(loanApplicationUseCase.create(invalid3))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_AMOUNT)
                .verify();

        verifyNoInteractions(loanTypeRepository, loanApplicationRepository);
    }

    @Test
    void shouldReturnErrorWhenTermMonthsIsNullOrInvalid() {
        LoanApplication invalid1 = buildValidLoanApplication();
        invalid1.setTermMonths(null);

        StepVerifier.create(loanApplicationUseCase.create(invalid1))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_TERMMONTHS)
                .verify();

        LoanApplication invalid2 = buildValidLoanApplication();
        invalid2.setTermMonths(0);

        StepVerifier.create(loanApplicationUseCase.create(invalid2))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_TERMMONTHS)
                .verify();

        LoanApplication invalid3 = buildValidLoanApplication();
        invalid3.setTermMonths(-5);

        StepVerifier.create(loanApplicationUseCase.create(invalid3))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_TERMMONTHS)
                .verify();

        verifyNoInteractions(loanTypeRepository, loanApplicationRepository);
    }

    @Test
    void shouldReturnErrorWhenLoanTypeDoesNotExist() {
        LoanApplication valid = buildValidLoanApplication();

        when(loanTypeRepository.existsByCode("CODE01")).thenReturn(Mono.just(false));

        StepVerifier.create(loanApplicationUseCase.create(valid))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_LOANTYPE)
                .verify();

        verify(loanTypeRepository).existsByCode("CODE01");
        verifyNoInteractions(loanApplicationRepository);
    }

    @Test
    void shouldSaveLoanApplicationWhenValid() {
        LoanApplication valid = buildValidLoanApplication();

        when(loanTypeRepository.existsByCode("CODE01")).thenReturn(Mono.just(true));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(loanApplicationUseCase.create(valid))
                .assertNext(saved -> {
                    assertEquals(LoanStatus.PENDING, saved.getStatus());
                    assertEquals("12345678", saved.getDocumentNumber());
                    assertEquals(BigDecimal.valueOf(1000), saved.getAmount());
                    assertEquals(12, saved.getTermMonths());
                })
                .verifyComplete();

        verify(loanTypeRepository).existsByCode("CODE01");
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }
}
