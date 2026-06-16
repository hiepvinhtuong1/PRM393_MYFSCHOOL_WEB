package vn.edu.fpt.myfptschool.parent.dto;

import vn.edu.fpt.myfptschool.parent.entity.Parent;
import vn.edu.fpt.myfptschool.student.dto.StudentSummaryResponse;

import java.util.List;

public record ParentResponse(
        Long id,
        String parentCode,
        String fullName,
        String dateOfBirth,
        String gender,
        String phone,
        String email,
        String username,
        boolean active,
        List<StudentSummaryResponse> children
) {
    public static ParentResponse from(Parent p) {
        return new ParentResponse(
                p.getId(),
                p.getParentCode(),
                p.getFullName(),
                p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null,
                p.getGender(),
                p.getPhone(),
                p.getEmail(),
                p.getUser() != null ? p.getUser().getUsername() : null,
                p.getUser() != null && p.getUser().isActive(),
                p.getChildren().stream().map(StudentSummaryResponse::from).toList()
        );
    }
}
