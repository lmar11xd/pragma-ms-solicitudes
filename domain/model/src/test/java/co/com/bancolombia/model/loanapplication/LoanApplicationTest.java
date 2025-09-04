package co.com.bancolombia.model.loanapplication;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class LoanApplicationTest {
    @Test
    void testNoArgsConstructorAndSetters() {
        LoanApplication loan = new LoanApplication();
        loan.setId("1");
        loan.setDocumentNumber("12345678");
        loan.setAmount(BigDecimal.valueOf(1000));
        loan.setTermMonths(12);
        loan.setLoanTypeCode("PERSONAL");
        loan.setComment("Test loan");
        loan.setCreatedAt(Instant.now());
        loan.setStatus(LoanStatus.PENDING);

        assertThat(loan.getId()).isEqualTo("1");
        assertThat(loan.getDocumentNumber()).isEqualTo("12345678");
        assertThat(loan.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(loan.getTermMonths()).isEqualTo(12);
        assertThat(loan.getLoanTypeCode()).isEqualTo("PERSONAL");
        assertThat(loan.getComment()).isEqualTo("Test loan");
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void testAllArgsConstructor() {
        Instant now = Instant.now();
        LoanApplication loan = new LoanApplication(
                "2",
                "87654321",
                BigDecimal.valueOf(5000),
                24,
                "MORTGAGE",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(1000),
                "House loan",
                now,
                LoanStatus.APPROVED
        );

        assertThat(loan.getId()).isEqualTo("2");
        assertThat(loan.getDocumentNumber()).isEqualTo("87654321");
        assertThat(loan.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(loan.getTermMonths()).isEqualTo(24);
        assertThat(loan.getLoanTypeCode()).isEqualTo("MORTGAGE");
        assertThat(loan.getInterestRate()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(loan.getMonthlyInstallment()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(loan.getComment()).isEqualTo("House loan");
        assertThat(loan.getCreatedAt()).isEqualTo(now);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    void testBuilder() {
        Instant now = Instant.now();

        LoanApplication loan = LoanApplication.builder()
                .id("3")
                .documentNumber("11223344")
                .amount(BigDecimal.valueOf(2000))
                .termMonths(6)
                .loanTypeCode("AUTO")
                .interestRate(new BigDecimal(10))
                .monthlyInstallment(new BigDecimal(1000))
                .comment("Car loan")
                .createdAt(now)
                .status(LoanStatus.REJECTED)
                .build();

        assertThat(loan.getId()).isEqualTo("3");
        assertThat(loan.getDocumentNumber()).isEqualTo("11223344");
        assertThat(loan.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(loan.getTermMonths()).isEqualTo(6);
        assertThat(loan.getLoanTypeCode()).isEqualTo("AUTO");
        assertThat(loan.getInterestRate()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(loan.getMonthlyInstallment()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(loan.getComment()).isEqualTo("Car loan");
        assertThat(loan.getCreatedAt()).isEqualTo(now);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.REJECTED);
    }

    @Test
    void testToBuilder() {
        LoanApplication original = LoanApplication.builder()
                .id("4")
                .documentNumber("44556677")
                .amount(BigDecimal.valueOf(3000))
                .termMonths(18)
                .loanTypeCode("BUSINESS")
                .interestRate(new BigDecimal(10))
                .monthlyInstallment(new BigDecimal(1000))
                .comment("Business loan")
                .createdAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build();

        LoanApplication copy = original.toBuilder()
                .amount(BigDecimal.valueOf(3500)) // se cambia solo el amount
                .build();

        assertThat(copy.getId()).isEqualTo(original.getId());
        assertThat(copy.getDocumentNumber()).isEqualTo(original.getDocumentNumber());
        assertThat(copy.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3500));
        assertThat(copy.getStatus()).isEqualTo(original.getStatus());
    }
}
