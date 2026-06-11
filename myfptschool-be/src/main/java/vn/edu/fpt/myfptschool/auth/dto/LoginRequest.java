package vn.edu.fpt.myfptschool.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Pattern(regexp = "^(mobile|web)$", message = "platform phải là 'mobile' hoặc 'web'")
    private String platform = "mobile";
}
