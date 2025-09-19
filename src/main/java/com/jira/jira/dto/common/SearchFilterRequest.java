package com.jira.jira.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchFilterRequest {

    @JsonProperty("search")
    private String search;

    @JsonProperty("filters")
    private Map<String, Object> filters;

    @JsonProperty("date_range")
    private DateRangeFilter dateRange;

    @JsonProperty("pagination")
    private PaginationRequest pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DateRangeFilter {
        @JsonProperty("field")
        private String field; // e.g., "createdAt", "updatedAt"

        @JsonProperty("from")
        private String from; // ISO date string

        @JsonProperty("to")
        private String to; // ISO date string
    }

    /**
     * Get filter value by key
     */
    public Object getFilter(String key) {
        return filters != null ? filters.get(key) : null;
    }

    /**
     * Get filter value as string
     */
    public String getFilterAsString(String key) {
        Object value = getFilter(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get filter value as boolean
     */
    public Boolean getFilterAsBoolean(String key) {
        Object value = getFilter(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    /**
     * Get filter value as list
     */
    @SuppressWarnings("unchecked")
    public List<String> getFilterAsList(String key) {
        Object value = getFilter(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }

    /**
     * Check if search is provided
     */
    public boolean hasSearch() {
        return search != null && !search.trim().isEmpty();
    }

    /**
     * Check if filters are provided
     */
    public boolean hasFilters() {
        return filters != null && !filters.isEmpty();
    }

    /**
     * Check if date range filter is provided
     */
    public boolean hasDateRange() {
        return dateRange != null && dateRange.getField() != null;
    }

    /**
     * Normalize search term (trim and lowercase)
     */
    public String getNormalizedSearch() {
        return hasSearch() ? search.trim().toLowerCase() : null;
    }
}
