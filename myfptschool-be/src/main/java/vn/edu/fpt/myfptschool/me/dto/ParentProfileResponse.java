package vn.edu.fpt.myfptschool.me.dto;

import lombok.Builder;
import lombok.Getter;
import vn.edu.fpt.myfptschool.parent.entity.Parent;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ParentProfileResponse {

    private String parentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private List<ChildSummary> children;

    public static ParentProfileResponse from(Parent parent) {
        List<ChildSummary> children = parent.getChildren().stream()
                .map(ChildSummary::from)
                .toList();

        return ParentProfileResponse.builder()
                .parentCode(parent.getParentCode())
                .fullName(parent.getFullName())
                .dateOfBirth(parent.getDateOfBirth())
                .gender(parent.getGender())
                .phone(parent.getPhone())
                .email(parent.getEmail())
                .children(children)
                .build();
    }
}
