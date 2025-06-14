package com.example.librarymanagement.dto;

import com.example.librarymanagement.entity.Loan;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {

    private Long id;

    @NotNull(message = "Loan date is required")
    @PastOrPresent(message = "Loan date cannot be in the future")
    private LocalDate loanDate;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    private LocalDate returnDate;

    @NotNull(message = "Status is required")
    private Loan.LoanStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Member ID is required")
    private Long memberId;

    private BookSummaryDto book;
    private MemberSummaryDto member;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookSummaryDto {
        private Long id;
        private String title;
        private String isbn;
        private String authorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummaryDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }
}