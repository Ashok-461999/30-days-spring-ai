package com.ashok.springagents.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SummarizeRequest(
        @NotBlank String text,
        @Min(1) @Max(20) int sentenceCount,
        @NotBlank String audience) {}