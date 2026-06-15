package vn.edu.fpt.myfptschool.timetable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateLessonRequest(
        @NotBlank @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Định dạng ngày phải là dd/MM/yyyy")
        String lessonDate,
        @NotNull Short startSlotId,
        @NotNull Short endSlotId,
        Long roomId
) {}
