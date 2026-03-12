package devsign_server.api.domain.project.controller;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.project.dto.CreateProjectRequest;
import devsign_server.api.domain.project.dto.ProjectDetailResponse;
import devsign_server.api.domain.project.dto.ProjectSummaryResponse;
import devsign_server.api.domain.project.dto.UpdateProjectStatusRequest;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import devsign_server.api.domain.project.service.ProjectService;
import devsign_server.api.fixture.MemberFixture;
import devsign_server.api.fixture.ProjectFixture;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProjectController 통합 테스트.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("ProjectController 통합 테스트")
class ProjectControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    // ================================
    // GET /api/projects
    // ================================

    @Test
    @DisplayName("프로젝트 목록 조회는 인증 없이도 200 OK를 반환한다")
    void getProjects_withoutAuth_returns200() throws Exception {
        // given
        Member author = MemberFixture.developer();
        Project project = ProjectFixture.recruiting(author);
        PageImpl<ProjectSummaryResponse> page = new PageImpl<>(
                List.of(ProjectSummaryResponse.from(project)), PageRequest.of(0, 10), 1
        );
        given(projectService.getProjects(any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].main_title").value(project.getMainTitle()));
    }

    @Test
    @DisplayName("keyword, status 필터로 프로젝트 목록을 조회할 수 있다")
    void getProjects_withFilters_returns200() throws Exception {
        // given
        given(projectService.getProjects(any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/api/projects")
                        .param("status", "RECRUITING")
                        .param("keyword", "앱")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    // ================================
    // GET /api/projects/{projectId}
    // ================================

    @Test
    @DisplayName("프로젝트 단건 조회는 인증 없이도 200 OK를 반환한다")
    void getProject_withoutAuth_returns200() throws Exception {
        // given
        Member author = MemberFixture.developer();
        Project project = ProjectFixture.recruiting(author);
        ProjectDetailResponse detail = ProjectDetailResponse.from(project, 10L);

        given(projectService.getProject(project.getProjectId())).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/projects/{id}", project.getProjectId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(project.getProjectId()))
                .andExpect(jsonPath("$.data.group_chat_id").value(10));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 404 Not Found를 반환한다")
    void getProject_notFound_returns404() throws Exception {
        // given
        given(projectService.getProject(999L))
                .willThrow(new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================
    // POST /api/projects
    // ================================

    @Test
    @DisplayName("인증된 사용자가 프로젝트 생성 시 201 Created를 반환한다")
    void createProject_authenticated_returns201() throws Exception {
        // given
        Member author = MemberFixture.developer();
        MemberDetails memberDetails = new MemberDetails(author);
        var auth = new UsernamePasswordAuthenticationToken(
                memberDetails, null, memberDetails.getAuthorities());

        CreateProjectRequest request = new CreateProjectRequest(
                "새 프로젝트", "서브 타이틀", "상세 내용",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), 2, 1
        );
        Project project = ProjectFixture.recruiting(author);
        ProjectDetailResponse response = ProjectDetailResponse.from(project, 10L);

        given(projectService.createProject(eq(author.getMemberId()), any(CreateProjectRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.main_title").value(project.getMainTitle()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 프로젝트 생성 시 401 Unauthorized를 반환한다")
    void createProject_unauthenticated_returns401() throws Exception {
        // given
        CreateProjectRequest request = new CreateProjectRequest(
                "새 프로젝트", null, "내용",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), 2, 1
        );

        // when & then
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("제목이 없는 요청으로 프로젝트 생성 시 400 Bad Request를 반환한다")
    void createProject_blankTitle_returns400() throws Exception {
        // given
        Member author = MemberFixture.developer();
        MemberDetails memberDetails = new MemberDetails(author);
        var auth = new UsernamePasswordAuthenticationToken(
                memberDetails, null, memberDetails.getAuthorities());

        CreateProjectRequest request = new CreateProjectRequest(
                "", null, "내용",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), 2, 1
        );

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================
    // PATCH /api/projects/{projectId}/status
    // ================================

    @Test
    @DisplayName("작성자가 프로젝트 상태 변경 시 200 OK를 반환한다")
    void updateStatus_byAuthor_returns200() throws Exception {
        // given
        Member author = MemberFixture.developer();
        MemberDetails memberDetails = new MemberDetails(author);
        var auth = new UsernamePasswordAuthenticationToken(
                memberDetails, null, memberDetails.getAuthorities());

        Project project = ProjectFixture.recruiting(author);
        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest(ProjectStatus.PROGRESS);
        Project progressed = ProjectFixture.create_with_status(project.getProjectId(), author, ProjectStatus.PROGRESS);
        ProjectDetailResponse response = ProjectDetailResponse.from(progressed, 10L);

        given(projectService.updateStatus(eq(author.getMemberId()), eq(project.getProjectId()), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/projects/{id}/status", project.getProjectId())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("progress"));
    }

    @Test
    @DisplayName("작성자가 아닌 경우 상태 변경 시 403 Forbidden을 반환한다")
    void updateStatus_notAuthor_returns403() throws Exception {
        // given
        Member other = MemberFixture.designer(2L);
        MemberDetails memberDetails = new MemberDetails(other);
        var auth = new UsernamePasswordAuthenticationToken(
                memberDetails, null, memberDetails.getAuthorities());

        UpdateProjectStatusRequest request = new UpdateProjectStatusRequest(ProjectStatus.PROGRESS);

        given(projectService.updateStatus(eq(other.getMemberId()), eq(1L), any()))
                .willThrow(new CustomException(ErrorCode.FORBIDDEN));

        // when & then
        mockMvc.perform(patch("/api/projects/{id}/status", 1L)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ================================
    // GET /api/projects/me
    // ================================

    @Test
    @DisplayName("인증된 사용자의 프로젝트 목록 조회 시 200 OK를 반환한다")
    void getMyProjects_authenticated_returns200() throws Exception {
        // given
        Member author = MemberFixture.developer();
        MemberDetails memberDetails = new MemberDetails(author);
        var auth = new UsernamePasswordAuthenticationToken(
                memberDetails, null, memberDetails.getAuthorities());

        Project project = ProjectFixture.recruiting(author);
        given(projectService.getMyProjects(author.getMemberId()))
                .willReturn(List.of(ProjectSummaryResponse.from(project)));

        // when & then
        mockMvc.perform(get("/api/projects/me")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].author_id").value(author.getMemberId()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 /me 조회 시 401 Unauthorized를 반환한다")
    void getMyProjects_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/projects/me"))
                .andExpect(status().isUnauthorized());
    }
}
