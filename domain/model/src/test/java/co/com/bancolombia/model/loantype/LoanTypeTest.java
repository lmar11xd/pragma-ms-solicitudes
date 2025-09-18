package co.com.bancolombia.model.loantype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanTypeTest {
    @Test
    void testNoArgsConstructorAndSetters() {
        LoanType loanType = new LoanType();

        loanType.setId("1");
        loanType.setCode("CODE01");
        loanType.setName("Personal Loan");
        loanType.setValidationAutomatic(true);

        assertEquals("1", loanType.getId());
        assertEquals("CODE01", loanType.getCode());
        assertEquals("Personal Loan", loanType.getName());
        assertEquals(true, loanType.getValidationAutomatic());
    }

    @Test
    void testAllArgsConstructor() {
        LoanType loanType = new LoanType("2", "CODE02", "Mortgage", false);

        assertEquals("2", loanType.getId());
        assertEquals("CODE02", loanType.getCode());
        assertEquals("Mortgage", loanType.getName());
        assertEquals(false, loanType.getValidationAutomatic());
    }

    @Test
    void testBuilder() {
        LoanType loanType = LoanType.builder()
                .id("3")
                .code("CODE03")
                .name("Car Loan")
                .validationAutomatic(true)
                .build();

        assertEquals("3", loanType.getId());
        assertEquals("CODE03", loanType.getCode());
        assertEquals("Car Loan", loanType.getName());
        assertEquals(true, loanType.getValidationAutomatic());
    }

    @Test
    void testToBuilder() {
        LoanType original = LoanType.builder()
                .id("4")
                .code("CODE04")
                .name("Education Loan")
                .validationAutomatic(true)
                .build();

        LoanType modified = original.toBuilder()
                .name("Updated Education Loan")
                .build();

        assertEquals("4", modified.getId());
        assertEquals("CODE04", modified.getCode());
        assertEquals("Updated Education Loan", modified.getName());
        assertEquals(true, modified.getValidationAutomatic());

        // Aseguramos que el original no cambió
        assertEquals("Education Loan", original.getName());
    }

}
