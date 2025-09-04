package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.CreateLoanApplicationRequest;
import co.com.bancolombia.api.dto.LoanApplicationDto;
import co.com.bancolombia.model.loanapplication.LoanApplication;
import co.com.bancolombia.model.loanapplication.LoanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationMapperTest {

    @Test
    void toDomain_ShouldMapDtoToDomain() {
        // Arrange
        CreateLoanApplicationRequest dto = new CreateLoanApplicationRequest(
                "123456789",
                new BigDecimal("5000"),
                24,
                "HIPOTECARIO",
                "Test Comment"
        );

        // Act
        LoanApplication domain = LoanApplicationMapper.toDomain(dto);

        // Assert
        assertThat(domain.getId()).isNull(); // id es null al crear
        assertThat(domain.getDocumentNumber()).isEqualTo("123456789");
        assertThat(domain.getAmount()).isEqualByComparingTo("5000");
        assertThat(domain.getTermMonths()).isEqualTo(24);
        assertThat(domain.getLoanTypeCode()).isEqualTo("HIPOTECARIO");
        assertThat(domain.getComment()).isEqualTo("Test Comment");
        assertThat(domain.getCreatedAt()).isNotNull(); // generado con Instant.now()
        assertThat(domain.getStatus()).isNull(); // en este mapper inicial está null
    }

    @Test
    void toDto_ShouldMapDomainToDto() {
        // Arrange
        Instant now = Instant.now();
        LoanApplication domain = new LoanApplication(
                "abc123",
                "987654321",
                new BigDecimal("10000"),
                36,
                "CONSUMO",
                "Another Comment",
                now,
                LoanStatus.PENDING
        );

        // Act
        LoanApplicationDto dto = LoanApplicationMapper.toDto(domain);

        // Assert
        assertThat(dto.id()).isEqualTo("abc123");
        assertThat(dto.documentNumber()).isEqualTo("987654321");
        assertThat(dto.amount()).isEqualByComparingTo("10000");
        assertThat(dto.termMonths()).isEqualTo(36);
        assertThat(dto.loanTypeCode()).isEqualTo("CONSUMO");
        assertThat(dto.comment()).isEqualTo("Another Comment");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.status()).isEqualTo("PENDING");
    }
}

