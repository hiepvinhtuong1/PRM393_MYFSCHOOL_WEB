package vn.edu.fpt.myfptschool.timetable.dto;

import jakarta.validation.constraints.NotNull;
import vn.edu.fpt.myfptschool.timetable.entity.LessonStatus;

public record UpdateLessonRequest(
        @NotNull LessonStatus status,
        Long roomId,
        String note
) {}
