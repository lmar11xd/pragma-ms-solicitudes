package co.com.bancolombia.model.exception;


import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void shouldCreateWithErrorCodeAndDefaultMessage() {
        DomainException ex = new DomainException(ErrorCode.REQUERID_DOCUMENTNUMBER);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REQUERID_DOCUMENTNUMBER);
        assertThat(ex.getMessage()).isEqualTo(ErrorCode.REQUERID_DOCUMENTNUMBER.getDefaultMessage());
        assertThat(ex.getDetails()).isEmpty();
    }

    @Test
    void shouldCreateWithCustomMessage() {
        DomainException ex = new DomainException(ErrorCode.INVALID_AMOUNT, "Custom amount error");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_AMOUNT);
        assertThat(ex.getMessage()).isEqualTo("Custom amount error");
        assertThat(ex.getDetails()).isEmpty();
    }

    @Test
    void shouldCreateWithErrorCodeAndDetails() {
        Map<String, Object> details = Map.of("field", "loanTypeCode", "value", "100");

        DomainException ex = new DomainException(ErrorCode.INVALID_LOANTYPE, details);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOANTYPE);
        assertThat(ex.getMessage()).isEqualTo(ErrorCode.INVALID_LOANTYPE.getDefaultMessage());
        assertThat(ex.getDetails()).containsEntry("field", "loanTypeCode")
                .containsEntry("value", "100");
    }
}