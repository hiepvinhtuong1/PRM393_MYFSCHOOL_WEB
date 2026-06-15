package vn.edu.fpt.myfptschool.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String role;
    private Long userId;
    private String username;
    private String fullName;
}
