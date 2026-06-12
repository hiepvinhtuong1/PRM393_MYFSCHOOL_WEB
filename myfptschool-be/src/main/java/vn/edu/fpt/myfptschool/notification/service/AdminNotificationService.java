package vn.edu.fpt.myfptschool.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.academic.entity.Classroom;
import vn.edu.fpt.myfptschool.academic.repository.ClassroomRepository;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.notification.dto.SendNotificationRequest;
import vn.edu.fpt.myfptschool.notification.dto.SendNotificationResponse;
import vn.edu.fpt.myfptschool.notification.entity.Notification;
import vn.edu.fpt.myfptschool.notification.entity.NotificationRecipient;
import vn.edu.fpt.myfptschool.notification.entity.NotificationTargetType;
import vn.edu.fpt.myfptschool.notification.repository.NotificationRecipientRepository;
import vn.edu.fpt.myfptschool.notification.repository.NotificationRepository;
import vn.edu.fpt.myfptschool.parent.repository.ParentRepository;
import vn.edu.fpt.myfptschool.student.repository.StudentRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;

    @Transactional
    public SendNotificationResponse send(String username, SendNotificationRequest request) {
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        validateTargetId(request);

        Notification notification = notificationRepository.save(
                Notification.create(request.title(), request.body(), request.category(),
                        request.targetType(), request.targetId(), sender)
        );

        List<User> recipients = resolveRecipients(request);

        List<NotificationRecipient> nrList = recipients.stream()
                .map(u -> NotificationRecipient.create(notification, u))
                .toList();
        notificationRecipientRepository.saveAll(nrList);

        return new SendNotificationResponse(notification.getId(), nrList.size());
    }

    private void validateTargetId(SendNotificationRequest request) {
        if (request.targetType() != NotificationTargetType.all && request.targetId() == null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "targetId bắt buộc khi targetType là " + request.targetType().name());
        }
    }

    private List<User> resolveRecipients(SendNotificationRequest request) {
        return switch (request.targetType()) {
            case individual -> {
                User user = userRepository.findById(request.targetId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Người dùng không tồn tại"));
                yield List.of(user);
            }
            case classroom -> {
                Classroom classroom = classroomRepository.findById(request.targetId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lớp học không tồn tại"));
                Set<User> users = new LinkedHashSet<>();
                users.addAll(studentRepository.findUsersByClassroom(classroom));
                users.addAll(parentRepository.findUsersByChildrenClassroom(classroom));
                yield new ArrayList<>(users);
            }
            case all -> userRepository.findAll();
        };
    }
}
