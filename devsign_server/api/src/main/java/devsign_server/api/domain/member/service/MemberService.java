package devsign_server.api.domain.member.service;

import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.chat.repository.GroupChatRepository;
import devsign_server.api.domain.chat.repository.MessageRepository;
import devsign_server.api.domain.member.dto.MemberResponse;
import devsign_server.api.domain.member.dto.UpdateProfileRequest;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.notification.repository.NotificationRepository;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.domain.review.repository.ReviewRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final ApplicantRepository applicantRepository;
    private final GroupChatRepository groupChatRepository;
    private final ProjectRepository projectRepository;

    public MemberResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, UpdateProfileRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateProfile(request.name(), request.profileData());
        return MemberResponse.from(member);
    }

    @Transactional
    public void deleteAccount(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 1. 알림 삭제
        notificationRepository.deleteByMemberMemberId(memberId);
        // 2. 리뷰 삭제 (reviewer 또는 reviewee)
        reviewRepository.deleteByReviewerOrReviewee(memberId);
        // 3. 본인이 보낸 메시지 삭제
        messageRepository.deleteByMemberMemberId(memberId);
        // 4. 본인 프로젝트의 채팅방 메시지 삭제 (다른 멤버가 보낸 것)
        messageRepository.deleteByGroupChatProjectMemberId(memberId);
        // 5. 본인 프로젝트에 지원한 신청자 삭제
        applicantRepository.deleteByProjectAuthorMemberId(memberId);
        // 6. 본인 프로젝트의 채팅방 삭제
        groupChatRepository.deleteByProjectMemberId(memberId);
        // 7. 본인의 지원 기록 삭제 (다른 프로젝트에 지원한 것)
        applicantRepository.deleteByMemberId(memberId);
        // 8. 본인 프로젝트 삭제
        projectRepository.deleteByMemberId(memberId);
        // 9. 회원 삭제
        memberRepository.deleteById(memberId);
    }
}
