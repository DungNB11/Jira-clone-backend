package com.jira.jira.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("total_elements")
    private Long totalElements;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("first")
    private Boolean first;

    @JsonProperty("last")
    private Boolean last;

    @JsonProperty("number_of_elements")
    private Integer numberOfElements;

    @JsonProperty("empty")
    private Boolean empty;

    @JsonProperty("sort_by")
    private String sortBy;

    @JsonProperty("sort_direction")
    private String sortDirection;

    /**
     * Create pagination response from data
     */
    public static <T> PaginationResponse<T> of(List<T> content, PaginationRequest request, Long totalElements) {
        int page = request.getPage();
        int size = request.getSize();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PaginationResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .sortBy(request.getSortBy())
                .sortDirection(request.getSortDirection().name())
                .build();
    }

    /**
     * Create empty pagination response
     */
    public static <T> PaginationResponse<T> empty(PaginationRequest request) {
        return PaginationResponse.<T>builder()
                .content(List.of())
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .numberOfElements(0)
                .empty(true)
                .sortBy(request.getSortBy())
                .sortDirection(request.getSortDirection().name())
                .build();
    }
}