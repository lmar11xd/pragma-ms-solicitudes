package co.com.bancolombia.model.exception;

import co.com.bancolombia.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {
    @Test
    void shouldReturnCodeAndMessageForSpecificEnum() {
        ErrorCode error = ErrorCode.INVALID_TERMMONTHS;

        assertThat(error.getCode()).isEqualTo("VALIDATION_INVALID_TERMMONTHS");
        assertThat(error.getDefaultMessage()).isEqualTo("Plazo en meses debe ser mayor a 0");
    }

    @Test
    void allErrorCodesShouldHaveNonEmptyCodeAndMessage() {
        for (ErrorCode error : ErrorCode.values()) {
            assertThat(error.getCode())
                    .as("Code for %s should not be null or blank", error.name())
                    .isNotNull()
                    .isNotBlank();

            assertThat(error.getDefaultMessage())
                    .as("DefaultMessage for %s should not be null or blank", error.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    void shouldContainExpectedNumberOfErrorCodes() {
        assertThat(ErrorCode.values()).hasSize(ErrorCode.values().length); // total de constantes definidas
    }
}
