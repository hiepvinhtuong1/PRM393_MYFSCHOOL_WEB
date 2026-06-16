package vn.edu.fpt.myfptschool.notification.dto;

import vn.edu.fpt.myfptschool.notification.entity.Notification;

public record SentNotificationResponse(
        Long id,
        String title,
        String body,
        String category,
        String targetType,
        Long targetId,
        int recipientCount,
        String createdAt
) {
    public static SentNotificationResponse from(Notification n, int recipientCount) {
        return new SentNotificationResponse(
                n.getId(), n.getTitle(), n.getBody(),
                n.getCategory().name(), n.getTargetType().name(), n.getTargetId(),
                recipientCount,
                n.getCreatedAt().toString()
        );
    }
}
