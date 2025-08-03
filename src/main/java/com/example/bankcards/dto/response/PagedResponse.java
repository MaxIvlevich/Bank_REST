package com.example.bankcards.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A standardized DTO for paginated API responses.
 *
 * @param <T> The type of the content in the page.
 */
public record PagedResponse<T> (
        List<T> content,
        @JsonProperty("page_number")
        int pageNumber,
        @JsonProperty("page_size")
        int pageSize,
        @JsonProperty("total_elements")
        long totalElements,
        @JsonProperty("total_pages")
        int totalPages,
        @JsonProperty("is_last")
        boolean isLast
) {
    /**
     * Factory method to create a PagedResponse from a Spring Data Page object.
     *
     * @param page The Page object from a repository query.
     * @param <T>  The type of the content.
     * @return A new PagedResponse instance.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
