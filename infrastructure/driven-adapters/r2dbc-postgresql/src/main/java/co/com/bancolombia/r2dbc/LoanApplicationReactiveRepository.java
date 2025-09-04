package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.LoanApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, String>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    @Query("""
        SELECT COALESCE(SUM(monthly_installment),0)
        FROM loan_applications
        WHERE status = 'APPROVED' AND document_number = $1
    """)
    Mono<BigDecimal> sumApprovedMonthlyDebtByDocument(String documentNumber);

}
