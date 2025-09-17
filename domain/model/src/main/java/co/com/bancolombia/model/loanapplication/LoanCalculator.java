package co.com.bancolombia.model.loanapplication;

import co.com.bancolombia.exception.DomainException;
import co.com.bancolombia.exception.ErrorCode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class LoanCalculator {

    /* Para calcular la cuota mensual (monthlyInstallment),
    * lo usual es aplicar la fórmula de anualidades del sistema francés (pagos fijos).

    Fórmula de la cuota mensual
        Cuota = (M*i)/(1-(1+i)^-n)

    Donde:
        M = Monto del préstamo (amount)
        i = Tasa de interés mensual (interestRate / 12)
        n = Plazo en meses (termMonths)
    * */
    public static BigDecimal calculateMonthlyInstallment(BigDecimal amount, BigDecimal annualRate, int termMonths) {
        if (amount == null || annualRate == null || termMonths <= 0) {
            throw new DomainException(ErrorCode.INVALID_LOAN_PARAMETERS);
        }

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64) // 12% -> 0.12
                .divide(BigDecimal.valueOf(12), MathContext.DECIMAL64); // -> 0.01

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // Caso especial: tasa 0%
            return amount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal numerator = amount.multiply(monthlyRate);
        BigDecimal denominator = BigDecimal.ONE.subtract(
                BigDecimal.ONE.add(monthlyRate).pow(-termMonths, MathContext.DECIMAL64)
        );

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
