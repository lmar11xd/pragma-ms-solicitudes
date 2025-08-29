package co.com.bancolombia.model.applicant;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Applicant {
    private String id; // UUID string
    private String names;
    private String lastNames;
    private String documentNumber;
    private LocalDate birthdate;
    private String address;
    private String phone;
    private String email;
    private BigDecimal baseSalary;
}
