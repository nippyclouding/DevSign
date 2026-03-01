package devsign_server.api.domain.applicant.service;

import devsign_server.api.domain.applicant.dto.ApplicantResponse;
import devsign_server.api.domain.applicant.dto.UpdateApplicantStatusRequest;
import devsign_server.api.domain.applicant.entity.Applicant;
import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.notification.entity.NotificationType;
import devsign_server.api.domain.notification.event.NotificationEvent;
import devsign_server.api.domain.project.dto.ProjectSummaryResponse;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApplicantResponse apply(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.isAuthor(memberId)) {
            throw new CustomException(ErrorCode.SELF_APPLICATION);
        }

        if (applicantRepository.existsByProjectProjectIdAndMemberMemberId(projectId, memberId)) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Applicant applicant = Applicant.builder()
                .project(project)
                .member(member)
                .build();

        applicantRepository.save(applicant);

        eventPublisher.publishEvent(new NotificationEvent(
                project.getMember().getMemberId(),
                NotificationType.NEW_APPLICANT,
                member.getName() + "님이 '" + project.getMainTitle() + "' 프로젝트에 지원했습니다.",
                projectId
        ));

        return ApplicantResponse.from(applicant);
    }

    public List<ApplicantResponse> getApplicants(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        boolean isAuthor = project.isAuthor(memberId);
        boolean isApprovedMember = applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                projectId, memberId, ApplicantStatus.APPROVED);

        if (!isAuthor && !isApprovedMember) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return applicantRepository.findByProjectProjectId(projectId)
                .stream()
                .map(ApplicantResponse::from)
                .toList();
    }

    @Transactional
    public ApplicantResponse updateStatus(Long memberId, Long applicantId, UpdateApplicantStatusRequest request) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICANT_NOT_FOUND));

        if (!applicant.getProject().isAuthor(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        applicant.updateStatus(request.status());

        NotificationType type = request.status() == ApplicantStatus.APPROVED
                ? NotificationType.APPROVED : NotificationType.REJECTED;
        String message = request.status() == ApplicantStatus.APPROVED
                ? "'" + applicant.getProject().getMainTitle() + "' 프로젝트 지원이 승인되었습니다."
                : "'" + applicant.getProject().getMainTitle() + "' 프로젝트 지원이 거절되었습니다.";

        eventPublisher.publishEvent(new NotificationEvent(
                applicant.getMember().getMemberId(),
                type,
                message,
                applicant.getProject().getProjectId()
        ));

        return ApplicantResponse.from(applicant);
    }

    public List<ProjectSummaryResponse> getJoinedProjects(Long memberId) {
        return applicantRepository.findByMemberMemberIdAndStatus(memberId, ApplicantStatus.APPROVED)
                .stream()
                .map(a -> ProjectSummaryResponse.from(a.getProject()))
                .toList();
    }
}
