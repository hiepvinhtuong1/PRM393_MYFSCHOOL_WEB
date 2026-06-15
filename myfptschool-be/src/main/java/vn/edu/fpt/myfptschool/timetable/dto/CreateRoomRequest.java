package vn.edu.fpt.myfptschool.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
        @NotBlank String code,
        @NotNull Long campusId
) {}
