package com.jira.jira.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationRequest {

    @JsonProperty("page")
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0;

    @JsonProperty("size")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;

    @JsonProperty("sort_by")
    private String sortBy;

    @JsonProperty("sort_direction")
    private SortDirection sortDirection = SortDirection.ASC;

    public enum SortDirection {
        ASC, DESC
    }

    /**
     * Validate and normalize pagination parameters
     */
    public void normalize() {
        if (this.page == null || this.page < 0) {
            this.page = 0;
        }
        if (this.size == null || this.size <= 0) {
            this.size = 20;
        }
        if (this.size > 100) {
            this.size = 100;
        }
        if (this.sortDirection == null) {
            this.sortDirection = SortDirection.ASC;
        }
    }

    /**
     * Get offset for database queries
     */
    public int getOffset() {
        return this.page * this.size;
    }

    /**
     * Get limit for database queries
     */
    public int getLimit() {
        return this.size;
    }
}