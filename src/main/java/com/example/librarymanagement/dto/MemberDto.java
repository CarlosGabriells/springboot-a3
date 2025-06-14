package com.example.librarymanagement.dto;

import com.example.librarymanagement.entity.Member;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @NotNull(message = "Membership date is required")
    @PastOrPresent(message = "Membership date cannot be in the future")
    private LocalDate membershipDate;

    @NotNull(message = "Status is required")
    private Member.MembershipStatus status;

    private List<LoanSummaryDto> loans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanSummaryDto {
        private Long id;
        private String bookTitle;
        private LocalDate loanDate;
        private LocalDate dueDate;
        private String status;
    }
}