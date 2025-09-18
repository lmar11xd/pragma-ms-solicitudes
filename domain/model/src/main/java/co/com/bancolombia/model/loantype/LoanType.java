package co.com.bancolombia.model.loantype;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {
    private String id;
    private String code;
    private String name;
    private Boolean validationAutomatic;
}
