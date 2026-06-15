package vn.edu.fpt.myfptschool.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.auth.dto.LoginRequest;
import vn.edu.fpt.myfptschool.auth.dto.LoginResponse;
import vn.edu.fpt.myfptschool.auth.dto.RefreshRequest;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.entity.UserSession;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.auth.repository.UserSessionRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.config.AppProperties;
import vn.edu.fpt.myfptschool.security.JwtTokenProvider;
import vn.edu.fpt.myfptschool.teacher.repository.TeacherRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;
    private final TeacherRepository teacherRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        // Xóa session cũ cùng platform (1 thiết bị = 1 session)
        userSessionRepository.deleteByUserAndPlatform(user, request.getPlatform());

        return buildResponse(user, request.getPlatform());
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        UserSession session = userSessionRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (session.isExpired()) {
            userSessionRepository.delete(session);
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = session.getUser();
        if (!user.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String platform = session.getPlatform();

        // Token rotation: xóa session cũ, tạo session mới
        userSessionRepository.delete(session);

        return buildResponse(user, platform);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        userSessionRepository.findByToken(request.getRefreshToken())
                .ifPresent(userSessionRepository::delete);
    }

    private LoginResponse buildResponse(User user, String platform) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        String refreshToken = UUID.randomUUID().toString();
        long refreshMs = appProperties.getJwt().getRefreshExpirationMs();
        LocalDateTime refreshExpiry = LocalDateTime.now(ZoneOffset.UTC)
                .plusSeconds(refreshMs / 1000);

        userSessionRepository.save(UserSession.create(user, refreshToken, platform, refreshExpiry));

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(appProperties.getJwt().getExpirationMs() / 1000)
                .role(user.getRole().name())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(resolveFullName(user))
                .build();
    }

    private String resolveFullName(User user) {
        if (user.getRole() == Role.TEACHER) {
            return teacherRepository.findByUser(user)
                    .map(t -> t.getFullName())
                    .orElse(user.getUsername());
        }
        return user.getUsername();
    }
}
