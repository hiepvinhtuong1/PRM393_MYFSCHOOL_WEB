package vn.edu.fpt.myfptschool.notification.dto;

import vn.edu.fpt.myfptschool.notification.entity.NotificationRecipient;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        String category,
        boolean isRead,
        String createdAt
) {
    public static NotificationResponse from(NotificationRecipient nr) {
        return new NotificationResponse(
                nr.getNotification().getId(),
                nr.getNotification().getTitle(),
                nr.getNotification().getBody(),
                nr.getNotification().getCategory().name(),
                nr.isRead(),
                nr.getNotification().getCreatedAt().toString()
        );
    }
}
