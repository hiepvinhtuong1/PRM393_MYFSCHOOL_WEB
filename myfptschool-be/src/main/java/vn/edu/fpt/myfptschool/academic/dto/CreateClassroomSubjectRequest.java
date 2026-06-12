package vn.edu.fpt.myfptschool.academic.dto;

import jakarta.validation.constraints.NotNull;

public record CreateClassroomSubjectRequest(
        @NotNull Long classroomId,
        @NotNull Long subjectId,
        @NotNull Long teacherId,
        @NotNull Long semesterId
) {}
