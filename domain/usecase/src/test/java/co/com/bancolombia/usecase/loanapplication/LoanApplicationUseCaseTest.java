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
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.notification.EventType;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoanApplicationUseCaseTest {

    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private ApplicantPort applicantPort;
    @Mock
    private SecurityPort securityPort;
    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private LoanApplicationUseCase useCase;

    private LoanApplication loanApplication;
    private Applicant applicant;
    private LoanType loanType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loanApplication = LoanApplication.builder()
                .loanTypeCode("PERSONAL")
                .amount(BigDecimal.valueOf(1000))
                .interestRate(BigDecimal.valueOf(10))
                .termMonths(12)
                .documentNumber("12345678")
                .build();

        applicant = Applicant.builder()
                .documentNumber("12345678")
                .email("user@test.com")
                .names("Juan")
                .lastNames("Pérez")
                .baseSalary(BigDecimal.valueOf(3000))
                .build();

        loanType = LoanType.builder()
                .code("PERSONAL")
                .validationAutomatic(true)
                .build();
    }

    @Test
    void shouldFailWhenDocumentIsBlank() {
        loanApplication.setDocumentNumber(" ");

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.REQUERID_DOCUMENTNUMBER)
                .verify();
    }

    @Test
    void shouldFailWhenAmountIsInvalid() {
        loanApplication.setAmount(BigDecimal.ZERO);

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_AMOUNT)
                .verify();
    }

    @Test
    void shouldFailWhenTermIsInvalid() {
        loanApplication.setTermMonths(0);

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_TERMMONTHS)
                .verify();
    }

    @Test
    void shouldFailWhenLoanTypeDoesNotExist() {
        when(loanTypeRepository.findByCode("PERSONAL")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.INVALID_LOANTYPE)
                .verify();
    }

    @Test
    void shouldFailWhenSecurityPortIsEmpty() {
        when(loanTypeRepository.findByCode("PERSONAL")).thenReturn(Mono.just(loanType));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.UNAUTHORIZED)
                .verify();
    }

    @Test
    void shouldFailWhenApplicantNotFound() {
        when(loanTypeRepository.findByCode("PERSONAL")).thenReturn(Mono.just(loanType));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.just("user@test.com"));
        when(applicantPort.findApplicantByDocumentNumber("12345678"))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.APPLICANT_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldFailWhenApplicantEmailDoesNotMatch() {
        when(loanTypeRepository.findByCode("PERSONAL")).thenReturn(Mono.just(loanType));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.just("other@test.com"));

        when(applicantPort.findApplicantByDocumentNumber("12345678"))
                .thenReturn(Mono.just(applicant));

        StepVerifier.create(useCase.create(loanApplication))
                .expectErrorMatches(ex -> ex instanceof DomainException &&
                        ((DomainException) ex).getErrorCode() == ErrorCode.UNAUTHORIZED_ACTION)
                .verify();
    }

    @Test
    void shouldSaveWhenValidApplication() {
        when(loanTypeRepository.findByCode("PERSONAL")).thenReturn(Mono.just(loanType));
        when(securityPort.getAuthenticatedEmail()).thenReturn(Mono.just("user@test.com"));

        when(applicantPort.findApplicantByDocumentNumber("12345678"))
                .thenReturn(Mono.just(applicant));

        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        when(loanApplicationRepository.sumApprovedMonthlyDebtByDocument("12345678"))
                .thenReturn(Mono.just(BigDecimal.ZERO));

        when(notificationPort.send(any(), eq(EventType.CAPACITY_LAMBDA))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(loanApplication))
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
        loan.setDocumentNumber("12345678");
        loan.setAmount(BigDecimal.valueOf(10000));
        loan.setTermMonths(12);
        loan.setLoanTypeCode("PERSONAL");
        loan.setInterestRate(BigDecimal.valueOf(12));
        loan.setStatus(LoanStatus.PENDING);

        when(loanApplicationRepository.countForFilters(anyList(), any(), any()))
                .thenReturn(Mono.just(1L));

        when(loanApplicationRepository.findForFilters(anyList(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(loan));

        when(applicantPort.findApplicantByDocumentNumber(eq("12345678")))
                .thenReturn(Mono.just(applicant));

        when(loanApplicationRepository.sumApprovedMonthlyDebtByDocument(eq("12345678")))
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
                    assertThat(item.email()).isEqualTo("user@test.com");
                    assertThat(item.documentNumber()).isEqualTo("12345678");
                    assertThat(item.loanType()).isEqualTo("PERSONAL");
                    assertThat(item.baseSalary()).isEqualByComparingTo("3000");
                    assertThat(item.totalMonthlyDebtApprovedRequest()).isEqualByComparingTo("500");
                })
                .verifyComplete();
    }
}
