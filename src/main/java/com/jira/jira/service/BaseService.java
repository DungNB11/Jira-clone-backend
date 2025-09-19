package com.jira.jira.service;

import com.jira.jira.dto.common.PaginationRequest;
import com.jira.jira.dto.common.PaginationResponse;
import com.jira.jira.dto.common.SearchFilterRequest;
import com.jira.jira.util.PaginationUtils;
import com.jira.jira.util.SearchFilterUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Base service class providing common pagination and filtering functionality
 */
public abstract class BaseService {

    /**
     * Apply search and filters to a list
     */
    protected <T> List<T> applySearchAndFilters(
            List<T> items,
            SearchFilterRequest request,
            List<Function<T, String>> searchFields,
            List<Predicate<T>> customFilters) {

        Stream<T> stream = items.stream();

        // Apply search
        if (request.hasSearch()) {
            stream = SearchFilterUtils.applyMultiFieldSearch(stream, request, searchFields);
        }

        // Apply custom filters
        if (customFilters != null && !customFilters.isEmpty()) {
            stream = SearchFilterUtils.applyFilters(stream, customFilters.toArray(new Predicate[0]));
        }

        return stream.toList();
    }

    /**
     * Apply pagination to a list
     */
    protected <T> List<T> applyPagination(List<T> items, PaginationRequest request) {
        return PaginationUtils.paginateList(items, request);
    }

    /**
     * Create paginated response
     */
    protected <T> PaginationResponse<T> createPaginatedResponse(
            List<T> items,
            SearchFilterRequest request,
            Long totalCount) {

        PaginationRequest paginationRequest = request.getPagination();
        if (paginationRequest == null) {
            paginationRequest = PaginationRequest.builder().build();
        }

        return PaginationUtils.createResponse(items, paginationRequest, totalCount);
    }

    /**
     * Get total count for pagination (override in subclasses for database queries)
     */
    protected <T> Long getTotalCount(List<T> items) {
        return (long) items.size();
    }

    /**
     * Process list with search, filters, and pagination
     */
    protected <T> PaginationResponse<T> processList(
            List<T> items,
            SearchFilterRequest request,
            List<Function<T, String>> searchFields,
            List<Predicate<T>> customFilters) {

        // Apply search and filters
        List<T> filteredItems = applySearchAndFilters(items, request, searchFields, customFilters);

        // Get total count before pagination
        Long totalCount = getTotalCount(filteredItems);

        // Apply pagination
        List<T> paginatedItems = applyPagination(filteredItems, request.getPagination());

        // Create response
        return createPaginatedResponse(paginatedItems, request, totalCount);
    }
}

