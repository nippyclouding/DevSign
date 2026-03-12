package devsign_server.api.domain.project.service;

import devsign_server.api.domain.chat.entity.GroupChat;
import devsign_server.api.domain.chat.repository.GroupChatRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.project.dto.CreateProjectRequest;
import devsign_server.api.domain.project.dto.ProjectDetailResponse;
import devsign_server.api.domain.project.dto.ProjectSummaryResponse;
import devsign_server.api.domain.project.dto.UpdateProjectStatusRequest;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.fixture.MemberFixture;
import devsign_server.api.fixture.ProjectFixture;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("ProjectService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupChatRepository groupChatRepository;

    // ================================
    // getProject
    // ================================

    @Test
    @DisplayName("프로젝트 단건 조회 성공 시 ProjectDetailResponse를 반환한다")
    void getProject_success() {
        // given
        Member author = MemberFixture.developer();
        Project project = ProjectFixture.recruiting(author);
        GroupChat groupChat = GroupChat.builder().project(project).build();
        ReflectionTestUtils.setField(groupChat, "groupChatId", 10L);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(groupChatRepository.findByProjectProjectId(project.getProjectId())).willReturn(Optional.of(groupChat));

        // when
        ProjectDetailResponse response = projectService.getProject(project.getProjectId());

        // then
        assertThat(response.id()).isEqualTo(project.getProjectId());
        assertThat(response.mainTitle()).isEqualTo(project.getMainTitle());
        assertThat(response.groupChatId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 PROJECT_NOT_FOUND 예외를 던진다")
    void getProject_notFound_throwsException() {
        // given
        given(projectRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getProject(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
    }

    // ================================
    // createProject
    // ================================

    @Test
    @DisplayName("프로젝트 생성 성공 시 GroupChat도 함께 생성하고 ProjectDetailResponse를 반환한다")
    void createProject_success() {
        // given
        Member author = MemberFixture.developer();
        CreateProjectRequest request = new CreateProjectRequest(
                "새 프로젝트", "서브 타이틀", "내용",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30),
                2, 1
        );

        given(memberRepository.findById(author.getMemberId())).willReturn(Optional.of(author));
        given(projectRepository.save(any(Project.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(groupChatRepository.save(any(GroupChat.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ProjectDetailResponse response = projectService.createProject(author.getMemberId(), request);

        // then
        assertThat(response.mainTitle()).isEqualTo(request.mainTitle());
        assertThat(response.status()).isEqualTo(ProjectStatus.RECRUITING);
        then(groupChatRepository).should().save(any(GroupChat.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원이 프로젝트 생성 시 MEMBER_NOT_FOUND 예외를 던진다")
    void createProject_memberNotFound_throwsException() {
        // given
        CreateProjectRequest request = new CreateProjectRequest(
                "제목", null, "내용",
                LocalDate.now(), LocalDate.now().plusMonths(1),
                1, 0
        );
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.createProject(999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }

    // ================================
    // updateStatus
    // ================================

    @Test
    @DisplayName("작성자가 프로젝트 상태 변경 성공 시 변경된 상태가 반영된 응답을 반환한다")
    void updateStatus_byAuthor_success() {
        // given
        Member author = MemberFixture.developer();
        Project project = ProjectFixture.recruiting(author);
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest(ProjectStatus.PROGRESS);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(groupChatRepository.findByProjectProjectId(project.getProjectId())).willReturn(Optional.empty());

        // when
        ProjectDetailResponse response = projectService.updateStatus(author.getMemberId(), project.getProjectId(), request);

        // then
        assertThat(response.status()).isEqualTo(ProjectStatus.PROGRESS);
    }

    @Test
    @DisplayName("작성자가 아닌 회원이 상태 변경 시 FORBIDDEN 예외를 던진다")
    void updateStatus_notAuthor_throwsForbidden() {
        // given
        Member author = MemberFixture.developer(1L);
        Member other = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest(ProjectStatus.PROGRESS);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> projectService.updateStatus(other.getMemberId(), project.getProjectId(), request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ================================
    // getMyProjects
    // ================================

    @Test
    @DisplayName("내 프로젝트 목록 조회 시 작성한 프로젝트 목록을 반환한다")
    void getMyProjects_success() {
        // given
        Member author = MemberFixture.developer();
        Project project1 = ProjectFixture.recruiting(1L, author);
        Project project2 = ProjectFixture.recruiting(2L, author);

        given(projectRepository.findByMemberMemberIdOrderByCreatedAtDesc(author.getMemberId()))
                .willReturn(List.of(project1, project2));

        // when
        List<ProjectSummaryResponse> responses = projectService.getMyProjects(author.getMemberId());

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ProjectSummaryResponse::authorId)
                .containsOnly(author.getMemberId());
    }
}
