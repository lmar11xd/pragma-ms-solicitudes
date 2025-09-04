package co.com.bancolombia.model.loanapplication;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {}