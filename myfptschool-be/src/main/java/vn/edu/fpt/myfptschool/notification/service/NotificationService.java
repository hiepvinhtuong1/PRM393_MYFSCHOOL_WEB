package vn.edu.fpt.myfptschool.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;
import vn.edu.fpt.myfptschool.common.exception.AppException;
import vn.edu.fpt.myfptschool.common.exception.ErrorCode;
import vn.edu.fpt.myfptschool.notification.dto.NotificationPageResponse;
import vn.edu.fpt.myfptschool.notification.dto.NotificationResponse;
import vn.edu.fpt.myfptschool.notification.entity.NotificationCategory;
import vn.edu.fpt.myfptschool.notification.repository.NotificationRecipientRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRecipientRepository recipientRepository;

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(String username, int page, int size, String category) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> responsePage;

        if (category != null && !category.isBlank()) {
            NotificationCategory cat = NotificationCategory.valueOf(category.toLowerCase());
            responsePage = recipientRepository
                    .findByUserAndCategory(user, cat, pageable)
                    .map(NotificationResponse::from);
        } else {
            responsePage = recipientRepository
                    .findByUser(user, pageable)
                    .map(NotificationResponse::from);
        }

        long unreadCount = recipientRepository.countUnread(user);

        return NotificationPageResponse.from(responsePage, unreadCount);
    }

    @Transactional
    public void markAsRead(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        int updated = recipientRepository.markAsRead(user, notificationId, LocalDateTime.now());
        if (updated == 0) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
    }
}
