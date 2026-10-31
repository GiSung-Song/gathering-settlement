package com.example.gathering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateExpenseRequest(
        @NotNull Long payerId,
        @Positive long amount,
        @NotBlank @Size(max = 200) String description,
        @NotEmpty List<@NotNull Long> participantIds) {
}
