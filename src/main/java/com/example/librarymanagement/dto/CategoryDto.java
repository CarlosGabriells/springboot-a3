package com.example.librarymanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private List<BookSummaryDto> books;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookSummaryDto {
        private Long id;
        private String title;
        private String isbn;
    }
}