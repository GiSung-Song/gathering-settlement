package com.example.gathering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGatheringRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull List<@NotBlank @Size(max = 50) String> nicknames) {
}
