package vn.edu.fpt.myfptschool.me.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.me.dto.ParentProfileResponse;
import vn.edu.fpt.myfptschool.me.dto.StudentProfileResponse;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    @Transactional(readOnly = true)
    public Object getProfile(String username) {
        log.info("[MeService] getProfile username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        log.info("[MeService] user found id={}, role={}", user.getId(), user.getRole());

        return switch (user.getRole()) {
            case STUDENT -> {
                boolean exists = studentRepository.findByUserWithClassroom(user).isPresent();
                log.info("[MeService] student record found={}", exists);
                yield studentRepository.findByUserWithClassroom(user)
                        .map(StudentProfileResponse::from)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            }
            case PARENT -> {
                boolean exists = parentRepository.findByUserWithChildren(user).isPresent();
                log.info("[MeService] parent record found={}", exists);
                yield parentRepository.findByUserWithChildren(user)
                        .map(ParentProfileResponse::from)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            }
            default -> throw new AppException(ErrorCode.NOT_FOUND);
        };
    }
}
