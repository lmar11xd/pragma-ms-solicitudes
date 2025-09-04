package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.security.SecurityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanApplicationUseCaseTest {

    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private ApplicantPort applicantPort;
    @Mock
    private SecurityPort securityPort;

    @InjectMocks
    private LoanApplicationUseCase useCase;

    private LoanApplication validLoan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validLoan = new LoanApplication();
        validLoan.setDocumentNumber("12345678");
        validLoan.setAmount(BigDecimal.valueOf(1000));
        validLoan.setTermMonths(12);
        validLoan.setLoanTypeCode("PERSONAL");
    }

    @Test
    void shouldFailWhenDocumentIsBlank() {
        validLoan.setDocumentNumber(" ");

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.REQUERID_DOCUMENTNUMBER)
                .verify();
    }

    @Test
    void shouldFailWhenAmountIsInvalid() {
        validLoan.setAmount(BigDecimal.ZERO);

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_AMOUNT)
                .verify();
    }

    @Test
    void shouldFailWhenTermIsInvalid() {
        validLoan.setTermMonths(0);

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_TERMMONTHS)
                .verify();
    }

    @Test
    void shouldFailWhenLoanTypeDoesNotExist() {
        when(loanTypeRepository.existsByCode("PERSONAL")).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_LOANTYPE)
                .verify();
    }

    @Test
    void shouldFailWhenSecurityPortIsEmpty() {
        when(loanTypeRepository.existsByCode("PERSONAL")).thenReturn(Mono.just(true));
        when(securityPort.getCurrentUserToken()).thenReturn(Mono.empty());
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.UNAUTHORIZED)
                .verify();
    }

    @Test
    void shouldFailWhenApplicantNotFound() {
        when(loanTypeRepository.existsByCode("PERSONAL")).thenReturn(Mono.just(true));
        when(securityPort.getCurrentUserToken()).thenReturn(Mono.just("token"));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.just("user@test.com"));
        when(applicantPort.findApplicantByDocumentNumber("12345678", "token"))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.APPLICANT_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldFailWhenApplicantEmailDoesNotMatch() {
        when(loanTypeRepository.existsByCode("PERSONAL")).thenReturn(Mono.just(true));
        when(securityPort.getCurrentUserToken()).thenReturn(Mono.just("token"));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.just("other@test.com"));

        Applicant applicant = new Applicant();
        applicant.setEmail("user@test.com");

        when(applicantPort.findApplicantByDocumentNumber("12345678", "token"))
                .thenReturn(Mono.just(applicant));

        StepVerifier.create(useCase.create(validLoan))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.UNAUTHORIZED_ACTION)
                .verify();
    }

    @Test
    void shouldSaveWhenValidApplication() {
        when(loanTypeRepository.existsByCode("PERSONAL")).thenReturn(Mono.just(true));
        when(securityPort.getCurrentUserToken()).thenReturn(Mono.just("token"));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.just("user@test.com"));

        Applicant applicant = new Applicant();
        applicant.setEmail("user@test.com");

        when(applicantPort.findApplicantByDocumentNumber("12345678", "token"))
                .thenReturn(Mono.just(applicant));

        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.create(validLoan))
                .assertNext(result -> {
                    assert result.getStatus() == LoanStatus.PENDING;
                    assert result.getAmount().equals(BigDecimal.valueOf(1000));
                })
                .verifyComplete();

        verify(loanApplicationRepository, times(1)).save(any(LoanApplication.class));
    }
}
