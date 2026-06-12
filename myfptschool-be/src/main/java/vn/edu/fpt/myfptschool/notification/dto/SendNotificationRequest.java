package vn.edu.fpt.myfptschool.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.edu.fpt.myfptschool.notification.entity.NotificationCategory;
import vn.edu.fpt.myfptschool.notification.entity.NotificationTargetType;

public record SendNotificationRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String body,
        @NotNull NotificationCategory category,
        @NotNull NotificationTargetType targetType,
        Long targetId
) {}
