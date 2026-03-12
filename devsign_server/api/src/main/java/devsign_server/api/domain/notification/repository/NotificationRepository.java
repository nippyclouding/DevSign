package devsign_server.api.domain.notification.repository;

import devsign_server.api.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByMemberMemberIdOrderByCreatedAtDesc(Long memberId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.member.memberId = :memberId AND n.isRead = false")
    void markAllAsRead(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.member.memberId = :memberId")
    void deleteByMemberMemberId(@Param("memberId") Long memberId);
}
