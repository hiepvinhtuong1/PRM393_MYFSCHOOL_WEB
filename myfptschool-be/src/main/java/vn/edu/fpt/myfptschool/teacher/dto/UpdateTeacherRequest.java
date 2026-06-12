package vn.edu.fpt.myfptschool.teacher.dto;

import jakarta.validation.constraints.*;

public record UpdateTeacherRequest(
        @NotBlank @Size(min = 2, max = 100)
        String fullName,

        @Pattern(regexp = "^0[3-9]\\d{8}$", message = "Số điện thoại không hợp lệ (phải 10 số, bắt đầu 03-09)")
        String phone,

        @Email(message = "Email không hợp lệ")
        String email,

        @NotNull Long campusId
) {}
