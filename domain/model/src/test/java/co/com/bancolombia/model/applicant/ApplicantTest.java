package co.com.bancolombia.model.applicant;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicantTest {
    @Test
    void testNoArgsConstructorAndSetters() {
        Applicant applicant = new Applicant();
        applicant.setId("123");
        applicant.setNames("Luis");
        applicant.setLastNames("Alvarado");
        applicant.setDocumentNumber("98765432");
        applicant.setBirthdate(LocalDate.of(1995, 5, 20));
        applicant.setAddress("Av. Siempre Viva 123");
        applicant.setPhone("999888777");
        applicant.setEmail("test@example.com");
        applicant.setBaseSalary(new BigDecimal("2500.50"));

        assertThat(applicant.getId()).isEqualTo("123");
        assertThat(applicant.getNames()).isEqualTo("Luis");
        assertThat(applicant.getLastNames()).isEqualTo("Alvarado");
        assertThat(applicant.getDocumentNumber()).isEqualTo("98765432");
        assertThat(applicant.getBirthdate()).isEqualTo(LocalDate.of(1995, 5, 20));
        assertThat(applicant.getAddress()).isEqualTo("Av. Siempre Viva 123");
        assertThat(applicant.getPhone()).isEqualTo("999888777");
        assertThat(applicant.getEmail()).isEqualTo("test@example.com");
        assertThat(applicant.getBaseSalary()).isEqualTo(new BigDecimal("2500.50"));
    }

    @Test
    void testAllArgsConstructor() {
        Applicant applicant = new Applicant(
                "456",
                "Carlos",
                "Ramirez",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "Calle Falsa 456",
                "111222333",
                "carlos@example.com",
                new BigDecimal("1800.00")
        );

        assertThat(applicant.getId()).isEqualTo("456");
        assertThat(applicant.getNames()).isEqualTo("Carlos");
        assertThat(applicant.getLastNames()).isEqualTo("Ramirez");
        assertThat(applicant.getDocumentNumber()).isEqualTo("12345678");
        assertThat(applicant.getBirthdate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(applicant.getAddress()).isEqualTo("Calle Falsa 456");
        assertThat(applicant.getPhone()).isEqualTo("111222333");
        assertThat(applicant.getEmail()).isEqualTo("carlos@example.com");
        assertThat(applicant.getBaseSalary()).isEqualTo(new BigDecimal("1800.00"));
    }

    @Test
    void testBuilderAndToBuilder() {
        Applicant applicant = Applicant.builder()
                .id("789")
                .names("Ana")
                .lastNames("Gonzales")
                .documentNumber("55566677")
                .birthdate(LocalDate.of(2000, 10, 10))
                .address("Av. Central 789")
                .phone("123456789")
                .email("ana@example.com")
                .baseSalary(new BigDecimal("3000.00"))
                .build();

        assertThat(applicant.getId()).isEqualTo("789");
        assertThat(applicant.getNames()).isEqualTo("Ana");
        assertThat(applicant.getLastNames()).isEqualTo("Gonzales");
        assertThat(applicant.getEmail()).isEqualTo("ana@example.com");

        // Usar toBuilder para modificar un campo
        Applicant modified = applicant.toBuilder()
                .names("Ana Maria")
                .build();

        assertThat(modified.getNames()).isEqualTo("Ana Maria");
        assertThat(modified.getLastNames()).isEqualTo("Gonzales");
    }
}
