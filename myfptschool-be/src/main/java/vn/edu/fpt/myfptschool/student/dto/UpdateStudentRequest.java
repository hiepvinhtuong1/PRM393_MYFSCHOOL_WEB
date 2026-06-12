package vn.edu.fpt.myfptschool.student.dto;

import jakarta.validation.constraints.*;

public record UpdateStudentRequest(
        @NotBlank @Size(min = 2, max = 100)
        String fullName,

        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Ngày sinh phải có định dạng dd/MM/yyyy")
        String dateOfBirth,

        @Pattern(regexp = "^(Nam|Nữ)$", message = "Giới tính chỉ chấp nhận Nam hoặc Nữ")
        String gender,

        @Pattern(regexp = "^0[3-9]\\d{8}$", message = "Số điện thoại không hợp lệ (phải 10 số, bắt đầu 03-09)")
        String phone,

        @Email(message = "Email không hợp lệ")
        String email,

        String photoUrl,

        @NotNull Long classroomId
) {}
