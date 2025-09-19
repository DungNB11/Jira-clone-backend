package com.jira.jira.util;

import com.jira.jira.dto.common.SearchFilterRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SearchFilterUtils {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static <T> Stream<T> applySearch(Stream<T> stream, SearchFilterRequest request, Function<T, String> searchFieldsExtractor) {
        if (!request.hasSearch()) {
            return stream;
        }

        String searchTerm = request.getNormalizedSearch();
        return stream.filter(item -> {
            String searchableText = searchFieldsExtractor.apply(item);
            return searchableText != null && searchableText.toLowerCase().contains(searchTerm);
        });
    }

    public static <T> Stream<T> applyMultiFieldSearch(Stream<T> stream, SearchFilterRequest request, List<Function<T, String>> searchFields) {
        if (!request.hasSearch()) {
            return stream;
        }

        String searchTerm = request.getNormalizedSearch();
        return stream.filter(item -> {
            return searchFields.stream()
                    .anyMatch(fieldExtractor -> {
                        String fieldValue = fieldExtractor.apply(item);
                        return fieldValue != null && fieldValue.toLowerCase().contains(searchTerm);
                    });
        });
    }

    public static <T> Stream<T> applyStringFilter(Stream<T> stream, String filterKey, Function<T, String> fieldExtractor) {
        return stream.filter(item -> {
            String fieldValue = fieldExtractor.apply(item);
            return fieldValue != null && fieldValue.equals(filterKey);
        });
    }

    public static <T> Stream<T> applyBooleanFilter(Stream<T> stream, Boolean filterValue, Function<T, Boolean> fieldExtractor) {
        if (filterValue == null) {
            return stream;
        }
        return stream.filter(item -> {
            Boolean fieldValue = fieldExtractor.apply(item);
            return filterValue.equals(fieldValue);
        });
    }

    public static <T> Stream<T> applyListFilter(Stream<T> stream, List<String> filterValues, Function<T, String> fieldExtractor) {
        if (filterValues == null || filterValues.isEmpty()) {
            return stream;
        }
        return stream.filter(item -> {
            String fieldValue = fieldExtractor.apply(item);
            return fieldValue != null && filterValues.contains(fieldValue);
        });
    }

    public static <T> Stream<T> applyDateRangeFilter(Stream<T> stream, SearchFilterRequest request, Function<T, LocalDateTime> dateFieldExtractor) {
        if (!request.hasDateRange()) {
            return stream;
        }

        SearchFilterRequest.DateRangeFilter dateRange = request.getDateRange();
        LocalDateTime fromDate = parseDate(dateRange.getFrom());
        LocalDateTime toDate = parseDate(dateRange.getTo());

        return stream.filter(item -> {
            LocalDateTime fieldDate = dateFieldExtractor.apply(item);
            if (fieldDate == null) {
                return false;
            }

            boolean afterFrom = fromDate == null || !fieldDate.isBefore(fromDate);
            boolean beforeTo = toDate == null || !fieldDate.isAfter(toDate);

            return afterFrom && beforeTo;
        });
    }

    public static <T> Stream<T> applyCustomFilter(Stream<T> stream, Predicate<T> filter) {
        return stream.filter(filter);
    }

    @SafeVarargs
    public static <T> Stream<T> applyFilters(Stream<T> stream, Predicate<T>... filters) {
        Stream<T> result = stream;
        for (Predicate<T> filter : filters) {
            result = result.filter(filter);
        }
        return result;
    }

    private static LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateString, ISO_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // Try parsing as date only
            try {
                return LocalDateTime.parse(dateString + "T00:00:00", ISO_DATE_FORMATTER);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public static <T> Predicate<T> createSearchPredicate(String searchTerm, List<Function<T, String>> searchFields) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return item -> true;
        }

        String normalizedSearch = searchTerm.trim().toLowerCase();
        return item -> searchFields.stream()
                .anyMatch(fieldExtractor -> {
                    String fieldValue = fieldExtractor.apply(item);
                    return fieldValue != null && fieldValue.toLowerCase().contains(normalizedSearch);
                });
    }

    public static <T> Predicate<T> createStringFilterPredicate(String filterValue, Function<T, String> fieldExtractor) {
        if (filterValue == null) {
            return item -> true;
        }
        return item -> {
            String fieldValue = fieldExtractor.apply(item);
            return filterValue.equals(fieldValue);
        };
    }

    public static <T> Predicate<T> createBooleanFilterPredicate(Boolean filterValue, Function<T, Boolean> fieldExtractor) {
        if (filterValue == null) {
            return item -> true;
        }
        return item -> {
            Boolean fieldValue = fieldExtractor.apply(item);
            return filterValue.equals(fieldValue);
        };
    }
}
