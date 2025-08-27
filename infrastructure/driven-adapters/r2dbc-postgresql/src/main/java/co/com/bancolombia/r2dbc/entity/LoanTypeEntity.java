package co.com.bancolombia.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("loan_types")
public record LoanTypeEntity(@Id String id, String code, String name) {
}