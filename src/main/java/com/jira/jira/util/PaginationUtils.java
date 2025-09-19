package com.jira.jira.util;

import com.jira.jira.dto.common.PaginationRequest;
import com.jira.jira.dto.common.PaginationResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

public class PaginationUtils {

    /**
     * Convert PaginationRequest to Spring Data Pageable
     */
    public static Pageable toPageable(PaginationRequest request) {
        if (request == null) {
            return PageRequest.of(0, 20);
        }

        request.normalize();

        Sort sort = Sort.unsorted();
        if (request.getSortBy() != null && !request.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = request.getSortDirection() == PaginationRequest.SortDirection.DESC
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, request.getSortBy());
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Create pagination response from list and total count
     */
    public static <T> PaginationResponse<T> createResponse(
            List<T> content,
            PaginationRequest request,
            Long totalElements) {

        if (request == null) {
            request = PaginationRequest.builder().build();
        }
        request.normalize();

        return PaginationResponse.of(content, request, totalElements);
    }

    /**
     * Apply pagination to a list
     */
    public static <T> List<T> paginateList(List<T> list, PaginationRequest request) {
        if (request == null) {
            return list;
        }

        request.normalize();

        int startIndex = request.getOffset();
        int endIndex = Math.min(startIndex + request.getSize(), list.size());

        if (startIndex >= list.size()) {
            return List.of();
        }

        return list.subList(startIndex, endIndex);
    }

    /**
     * Apply sorting to a list
     */
    public static <T> List<T> sortList(List<T> list, PaginationRequest request, Function<T, Comparable> keyExtractor) {
        if (request == null || request.getSortBy() == null || list.isEmpty()) {
            return list;
        }

        request.normalize();

        return list.stream()
                .sorted((a, b) -> {
                    Comparable keyA = keyExtractor.apply(a);
                    Comparable keyB = keyExtractor.apply(b);

                    if (keyA == null && keyB == null) return 0;
                    if (keyA == null) return 1;
                    if (keyB == null) return -1;

                    int result = keyA.compareTo(keyB);
                    return request.getSortDirection() == PaginationRequest.SortDirection.DESC ? -result : result;
                })
                .toList();
    }

    /**
     * Calculate total pages
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Check if page is valid
     */
    public static boolean isValidPage(int page, int totalPages) {
        return page >= 0 && page < totalPages;
    }
}
