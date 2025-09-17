package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.LoanTypeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanTypeReactiveRepository extends ReactiveCrudRepository<LoanTypeEntity, String>, ReactiveQueryByExampleExecutor<LoanTypeEntity> {
    Mono<LoanTypeEntity> findByCode(String code);

    @Query("select exists(select 1 from loan_types where code = :code)")
    Mono<Boolean> existsByCode(String code);

    @Query("select validation_automatic from loan_types where code = :code")
    Mono<Boolean> isValidationAutomatic(String code);
}
