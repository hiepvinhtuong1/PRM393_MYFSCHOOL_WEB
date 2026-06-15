package vn.edu.fpt.myfptschool.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.createdBy = :sender
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findBySender(@Param("sender") User sender, Pageable pageable);

    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr WHERE nr.notification = :notification")
    int countRecipients(@Param("notification") Notification notification);
}
