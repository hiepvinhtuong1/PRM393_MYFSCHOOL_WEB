package vn.edu.fpt.myfptschool.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.notification.entity.NotificationRecipient;

import java.time.LocalDateTime;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {

    @Query(
        value = """
            SELECT nr FROM NotificationRecipient nr
            JOIN FETCH nr.notification n
            WHERE nr.user = :user
            ORDER BY n.createdAt DESC
            """,
        countQuery = "SELECT COUNT(nr) FROM NotificationRecipient nr WHERE nr.user = :user"
    )
    Page<NotificationRecipient> findByUser(@Param("user") User user, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE NotificationRecipient nr
        SET nr.isRead = true, nr.readAt = :now
        WHERE nr.user = :user
        AND nr.notification.id = :notificationId
        """)
    int markAsRead(
            @Param("user") User user,
            @Param("notificationId") Long notificationId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr WHERE nr.user = :user AND nr.isRead = false")
    long countUnread(@Param("user") User user);
}
