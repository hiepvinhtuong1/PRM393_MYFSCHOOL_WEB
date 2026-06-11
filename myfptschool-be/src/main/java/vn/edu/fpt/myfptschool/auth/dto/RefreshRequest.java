package vn.edu.fpt.myfptschool.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshRequest {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}
