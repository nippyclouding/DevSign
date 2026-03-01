package devsign_server.api.domain.notification.service;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.notification.dto.NotificationResponse;
import devsign_server.api.domain.notification.entity.Notification;
import devsign_server.api.domain.notification.entity.NotificationType;
import devsign_server.api.domain.notification.event.NotificationEvent;
import devsign_server.api.domain.notification.repository.NotificationRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        Member receiver = memberRepository.findById(event.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = Notification.builder()
                .member(receiver)
                .type(event.getType())
                .message(event.getMessage())
                .relatedId(event.getRelatedId())
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotifications(Long memberId) {
        return notificationRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getMember().getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    @Transactional
    public void sendReviewRequest(Long projectId, String projectTitle, List<Long> memberIds) {
        memberIds.forEach(memberId -> {
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                Notification notification = Notification.builder()
                        .member(member)
                        .type(NotificationType.REVIEW_REQUEST)
                        .message("'" + projectTitle + "' 프로젝트가 완료되었습니다. 팀원 리뷰를 작성해주세요.")
                        .relatedId(projectId)
                        .build();
                notificationRepository.save(notification);
            }
        });
    }
}
