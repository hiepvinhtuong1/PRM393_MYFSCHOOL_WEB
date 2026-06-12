package vn.edu.fpt.myfptschool.parent.dto;

import jakarta.validation.constraints.*;

public record CreateParentRequest(
        @NotBlank @Size(min = 3, max = 20)
        @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã phụ huynh chỉ được chứa chữ hoa và số")
        String parentCode,

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

        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[a-z0-9_]+$", message = "Tên đăng nhập chỉ chứa chữ thường, số và dấu _")
        String username,

        @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
        String password
) {}
