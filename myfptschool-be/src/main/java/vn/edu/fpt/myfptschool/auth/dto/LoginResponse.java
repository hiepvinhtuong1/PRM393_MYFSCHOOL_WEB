package vn.edu.fpt.myfptschool.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String token;
    private String role;
    private long expiresAt;
}
