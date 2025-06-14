package com.example.librarymanagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDto {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Size(max = 1000, message = "Biography must not exceed 1000 characters")
    private String biography;

    private List<BookSummaryDto> books;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookSummaryDto {
        private Long id;
        private String title;
        private String isbn;
        private LocalDate publicationDate;
    }
}