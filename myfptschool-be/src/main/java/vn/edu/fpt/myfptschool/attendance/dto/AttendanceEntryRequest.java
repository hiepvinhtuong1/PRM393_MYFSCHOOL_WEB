package vn.edu.fpt.myfptschool.attendance.dto;

import jakarta.validation.constraints.NotNull;
import vn.edu.fpt.myfptschool.attendance.entity.AttendanceStatus;

public record AttendanceEntryRequest(
        @NotNull Long studentId,
        @NotNull AttendanceStatus status,
        String note
) {}
