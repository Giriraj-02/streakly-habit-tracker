package com.streakly.dto;

import com.streakly.model.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HabitRequest(
        @NotBlank(message = "Habit name is required")
        @Size(max = 100, message = "Habit name must be 100 characters or less")
        String name,

        @NotNull(message = "Frequency is required")
        Frequency frequency
) {
}
