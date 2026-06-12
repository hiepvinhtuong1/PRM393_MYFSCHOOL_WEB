package vn.edu.fpt.myfptschool.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.common.entity.BaseEntity;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public static Notification create(String title, String body,
                                      NotificationCategory category,
                                      NotificationTargetType targetType,
                                      Long targetId, User createdBy) {
        Notification n = new Notification();
        n.title = title;
        n.body = body;
        n.category = category;
        n.targetType = targetType;
        n.targetId = targetId;
        n.createdBy = createdBy;
        return n;
    }
}
