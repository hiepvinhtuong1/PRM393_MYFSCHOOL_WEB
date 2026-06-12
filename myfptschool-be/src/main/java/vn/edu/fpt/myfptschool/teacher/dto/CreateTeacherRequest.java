package vn.edu.fpt.myfptschool.teacher.dto;

import jakarta.validation.constraints.*;

public record CreateTeacherRequest(
        @NotBlank @Size(min = 2, max = 100)
        String fullName,

        @Pattern(regexp = "^0[3-9]\\d{8}$", message = "Số điện thoại không hợp lệ (phải 10 số, bắt đầu 03-09)")
        String phone,

        @Email(message = "Email không hợp lệ")
        String email,

        @NotNull Long campusId,

        @NotBlank
        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[a-z0-9_]+$", message = "Tên đăng nhập chỉ chứa chữ thường, số và dấu _")
        String username,

        @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
        String password
) {}
