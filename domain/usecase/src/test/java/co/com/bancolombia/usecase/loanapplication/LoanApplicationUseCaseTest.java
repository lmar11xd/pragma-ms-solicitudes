package co.com.bancolombia.usecase.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.model.applicant.Applicant;
import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.AdvisorReviewItem;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import co.com.bancolombia.model.loanapplication.Page;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.security.SecurityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        validLoan.setInterestRate(BigDecimal.valueOf(10));
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

    @Test
    void listPendingApplicationsShouldReturnPageResponse() {
        // Arrange
        LoanApplication loan = new LoanApplication();
        loan.setDocumentNumber("123");
        loan.setAmount(BigDecimal.valueOf(10000));
        loan.setTermMonths(12);
        loan.setLoanTypeCode("HOME");
        loan.setInterestRate(BigDecimal.valueOf(12));
        loan.setStatus(LoanStatus.PENDING);

        Applicant applicant = new Applicant(
                "1", "Juan", "Pérez", "123",
                LocalDate.of(1990, 5, 20),
                "Calle 123", "987654321",
                "juan@example.com",
                BigDecimal.valueOf(3000)
        );

        when(loanApplicationRepository.countForFilters(anyList(), any(), any()))
                .thenReturn(Mono.just(1L));

        when(loanApplicationRepository.findForFilters(anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(loan));

        when(securityPort.getCurrentUserToken()).thenReturn(Mono.just("mock-token"));
        when(applicantPort.findApplicantByDocumentNumber(eq("123"), eq("mock-token")))
                .thenReturn(Mono.just(applicant));
        when(loanApplicationRepository.sumApprovedMonthlyDebtByDocument(eq("123")))
                .thenReturn(Mono.just(BigDecimal.valueOf(500)));

        // Act
        Mono<Page<AdvisorReviewItem>> result = useCase
                .listApplications(0, 10, null, null, null);

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.totalElements()).isEqualTo(1L);
                    assertThat(page.content()).hasSize(1);

                    AdvisorReviewItem item = page.content().getFirst();
                    assertThat(item.names()).isEqualTo("Juan Pérez");
                    assertThat(item.email()).isEqualTo("juan@example.com");
                    assertThat(item.documentNumber()).isEqualTo("123");
                    assertThat(item.loanType()).isEqualTo("HOME");
                    assertThat(item.baseSalary()).isEqualByComparingTo("3000");
                    assertThat(item.totalMonthlyDebtApprovedRequest()).isEqualByComparingTo("500");
                })
                .verifyComplete();
    }

    @Test
    void calculateMonthlyInstallmentShouldCalculateCorrectly() throws Exception {
        // Access private method via reflection
        var method = LoanApplicationUseCase.class
                .getDeclaredMethod("calculateMonthlyInstallment", BigDecimal.class, BigDecimal.class, int.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(useCase, BigDecimal.valueOf(10000), BigDecimal.valueOf(12), 12);

        assertThat(result).isNotNull();
        assertThat(result.doubleValue()).isGreaterThan(0);
    }

    @Test
    void calculateMonthlyInstallmentShouldHandleZeroInterestRate() throws Exception {
        var method = LoanApplicationUseCase.class
                .getDeclaredMethod("calculateMonthlyInstallment", BigDecimal.class, BigDecimal.class, int.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(useCase, BigDecimal.valueOf(1200), BigDecimal.ZERO, 12);

        assertThat(result).isEqualByComparingTo("100.00"); // 1200 / 12
    }

    @Test
    void calculateMonthlyInstallmentShouldThrowExceptionForInvalidParams() throws Exception {
        var method = LoanApplicationUseCase.class
                .getDeclaredMethod("calculateMonthlyInstallment", BigDecimal.class, BigDecimal.class, int.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(useCase, null, BigDecimal.TEN, 12));
        assertThrows(Exception.class, () -> method.invoke(useCase, BigDecimal.TEN, null, 12));
        assertThrows(Exception.class, () -> method.invoke(useCase, BigDecimal.TEN, BigDecimal.ONE, 0));
    }
}
