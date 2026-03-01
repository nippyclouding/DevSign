package devsign_server.api.domain.project.service;

import devsign_server.api.domain.chat.entity.GroupChat;
import devsign_server.api.domain.chat.repository.GroupChatRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.project.dto.*;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final GroupChatRepository groupChatRepository;

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

        GroupChat groupChat = GroupChat.builder()
                .project(project)
                .build();
        groupChatRepository.save(groupChat);

        return ProjectDetailResponse.from(project, groupChat.getGroupChatId());
    }

    @Transactional
    public ProjectDetailResponse updateStatus(Long memberId, Long projectId, UpdateProjectStatusRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isAuthor(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        project.updateStatus(request.status());

        Long groupChatId = groupChatRepository.findByProjectProjectId(projectId)
                .map(GroupChat::getGroupChatId)
                .orElse(null);
        return ProjectDetailResponse.from(project, groupChatId);
    }

    public List<ProjectSummaryResponse> getMyProjects(Long memberId) {
        return projectRepository.findByMemberMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .toList();
    }
}
