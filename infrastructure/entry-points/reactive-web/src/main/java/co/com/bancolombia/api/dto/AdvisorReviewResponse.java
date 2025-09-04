package co.com.bancolombia.api.dto;

import co.com.bancolombia.model.loanapplication.AdvisorReviewItem;
import lombok.Builder;

import java.util.List;

@Builder
public record AdvisorReviewResponse(
        List<AdvisorReviewItem> content,
        long totalElements,
        int page,
        int size
) {}