package vn.edu.fpt.myfptschool.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_CREDENTIALS(401, "Tên đăng nhập hoặc mật khẩu không đúng"),
    INVALID_REFRESH_TOKEN(401, "Refresh token không hợp lệ hoặc đã hết hạn"),
    ACCOUNT_DISABLED(403, "Tài khoản đã bị vô hiệu hóa"),
    UNAUTHORIZED(401, "Chưa đăng nhập"),
    FORBIDDEN(403, "Không có quyền truy cập"),
    NOT_FOUND(404, "Không tìm thấy"),
    VALIDATION_FAILED(400, "Dữ liệu không hợp lệ"),
    INTERNAL_ERROR(500, "Lỗi hệ thống");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
