package devsign_server.api.domain.project.service;

import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.chat.entity.GroupChat;
import devsign_server.api.domain.chat.repository.GroupChatRepository;
import devsign_server.api.domain.chat.repository.MessageRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.project.dto.*;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.domain.review.repository.ReviewRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final GroupChatRepository groupChatRepository;
    private final ApplicantRepository applicantRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;

    public Page<ProjectSummaryResponse> getProjects(ProjectStatus status, String section, String keyword, Pageable pageable) {
        return projectRepository.findAllWithFilter(status, section, keyword, pageable)
                .map(ProjectSummaryResponse::from);
    }

    public ProjectDetailResponse getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        Long groupChatId = groupChatRepository.findByProjectProjectId(projectId)
                .map(GroupChat::getGroupChatId)
                .orElse(null);
        return ProjectDetailResponse.from(project, groupChatId);
    }

    @Transactional
    public ProjectDetailResponse createProject(Long memberId, CreateProjectRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Project project = Project.builder()
                .member(member)
                .mainTitle(request.mainTitle())
                .subtitle(request.subtitle())
                .content(request.content())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .neededDevelopers(request.neededDevelopers())
                .neededDesigners(request.neededDesigners())
                .build();

        projectRepository.save(project);

        GroupChat groupChat = GroupChat.builder().project(project).build();
        groupChatRepository.save(groupChat);

        return ProjectDetailResponse.from(project, groupChat.getGroupChatId());
    }

    @Transactional
    public ProjectDetailResponse updateProject(Long memberId, Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isAuthor(memberId)) throw new CustomException(ErrorCode.FORBIDDEN);
        if (project.getStatus() != ProjectStatus.RECRUITING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        project.update(request.mainTitle(), request.subtitle(), request.content(),
                request.startDate(), request.endDate(),
                request.neededDevelopers(), request.neededDesigners());

        Long groupChatId = groupChatRepository.findByProjectProjectId(projectId)
                .map(GroupChat::getGroupChatId).orElse(null);
        return ProjectDetailResponse.from(project, groupChatId);
    }

    @Transactional
    public ProjectDetailResponse updateStatus(Long memberId, Long projectId, UpdateProjectStatusRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isAuthor(memberId)) throw new CustomException(ErrorCode.FORBIDDEN);

        validateStatusTransition(project.getStatus(), request.status());
        project.updateStatus(request.status());

        Long groupChatId = groupChatRepository.findByProjectProjectId(projectId)
                .map(GroupChat::getGroupChatId).orElse(null);
        return ProjectDetailResponse.from(project, groupChatId);
    }

    /** 상태 전환 규칙: RECRUITING → PROGRESS → COMPLETED (역전환 불가) */
    private void validateStatusTransition(ProjectStatus current, ProjectStatus next) {
        boolean valid = switch (current) {
            case RECRUITING -> next == ProjectStatus.PROGRESS;
            case PROGRESS -> next == ProjectStatus.COMPLETED;
            case COMPLETED -> false;
        };
        if (!valid) throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Transactional
    public void deleteProject(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isAuthor(memberId)) throw new CustomException(ErrorCode.FORBIDDEN);
        if (project.getStatus() != ProjectStatus.RECRUITING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        reviewRepository.deleteByProjectId(projectId);
        messageRepository.deleteByProjectId(projectId);
        applicantRepository.deleteByProjectId(projectId);
        groupChatRepository.deleteByProjectId(projectId);
        projectRepository.deleteById(projectId);
    }

    public ProjectStatsResponse getStats() {
        long active = projectRepository.countActiveProjects();
        long today = projectRepository.countTodayProjects(LocalDateTime.now().toLocalDate().atStartOfDay());
        return new ProjectStatsResponse(active, today);
    }

    public MembershipResponse getMembership(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        boolean isAuthor = project.isAuthor(memberId);
        boolean isApproved = applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                projectId, memberId, ApplicantStatus.APPROVED);
        var myApp = applicantRepository.findByProjectProjectIdAndMemberMemberId(projectId, memberId).orElse(null);
        String applicationStatus = myApp != null ? myApp.getStatus().name().toLowerCase() : null;
        Long applicationId = myApp != null ? myApp.getApplicantId() : null;
        return new MembershipResponse(isAuthor, isApproved, applicationStatus, applicationId);
    }

    public List<ProjectSummaryResponse> getMyProjects(Long memberId) {
        return projectRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .toList();
    }
}
