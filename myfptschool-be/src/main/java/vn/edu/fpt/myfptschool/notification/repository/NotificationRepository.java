package vn.edu.fpt.myfptschool.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.myfptschool.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
