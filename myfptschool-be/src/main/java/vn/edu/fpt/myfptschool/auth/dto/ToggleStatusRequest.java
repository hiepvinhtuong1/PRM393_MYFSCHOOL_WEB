package vn.edu.fpt.myfptschool.auth.dto;

import jakarta.validation.constraints.NotNull;

public record ToggleStatusRequest(@NotNull Boolean active) {}
