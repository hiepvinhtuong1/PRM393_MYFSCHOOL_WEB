package vn.edu.fpt.myfptschool.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_recipients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationRecipient extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public static NotificationRecipient create(Notification notification, User user) {
        NotificationRecipient nr = new NotificationRecipient();
        nr.notification = notification;
        nr.user = user;
        nr.isRead = false;
        return nr;
    }
}
