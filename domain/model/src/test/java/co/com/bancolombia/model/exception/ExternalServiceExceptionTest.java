package co.com.bancolombia.model.exception;

import co.com.bancolombia.exception.ErrorCode;
import co.com.bancolombia.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExternalServiceExceptionTest {

    @Test
    void shouldStoreErrorCodeAndMessage() {
        // given
        ErrorCode errorCode = ErrorCode.APPLICANT_NOT_FOUND;

        // when
        ExternalServiceException exception = new ExternalServiceException(errorCode);

        // then
        assertEquals(errorCode, exception.errorCode);
        assertEquals(errorCode.getDefaultMessage(), exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }
}