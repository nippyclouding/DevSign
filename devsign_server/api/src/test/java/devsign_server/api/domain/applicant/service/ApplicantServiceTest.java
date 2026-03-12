package devsign_server.api.domain.applicant.service;

import devsign_server.api.domain.applicant.dto.ApplicantResponse;
import devsign_server.api.domain.applicant.dto.UpdateApplicantStatusRequest;
import devsign_server.api.domain.applicant.entity.Applicant;
import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.notification.event.NotificationEvent;
import devsign_server.api.domain.project.entity.Project;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("ApplicantService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {

    @InjectMocks
    private ApplicantService applicantService;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    // ================================
    // apply
    // ================================

    @Test
    @DisplayName("프로젝트 지원 성공 시 ApplicantResponse를 반환하고 알림 이벤트를 발행한다")
    void apply_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member applicant = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberId(
                project.getProjectId(), applicant.getMemberId())).willReturn(false);
        given(memberRepository.findById(applicant.getMemberId())).willReturn(Optional.of(applicant));
        given(applicantRepository.save(any(Applicant.class))).willAnswer(invocation -> {
            Applicant saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "applicantId", 1L);
            return saved;
        });

        // when
        ApplicantResponse response = applicantService.apply(applicant.getMemberId(), project.getProjectId());

        // then
        assertThat(response.status()).isEqualTo(ApplicantStatus.PENDING);
        then(eventPublisher).should().publishEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("자신의 프로젝트에 지원 시 SELF_APPLICATION 예외를 던진다")
    void apply_selfApplication_throwsException() {
        // given
        Member author = MemberFixture.developer(1L);
        Project project = ProjectFixture.recruiting(author);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> applicantService.apply(author.getMemberId(), project.getProjectId()))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SELF_APPLICATION));
    }

    @Test
    @DisplayName("중복 지원 시 DUPLICATE_APPLICATION 예외를 던진다")
    void apply_duplicate_throwsException() {
        // given
        Member author = MemberFixture.developer(1L);
        Member applicant = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberId(
                project.getProjectId(), applicant.getMemberId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> applicantService.apply(applicant.getMemberId(), project.getProjectId()))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_APPLICATION));
    }

    // ================================
    // getApplicants
    // ================================

    @Test
    @DisplayName("작성자는 지원자 목록을 조회할 수 있다")
    void getApplicants_byAuthor_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member applicantMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);
        Applicant applicant = buildApplicant(1L, project, applicantMember);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                project.getProjectId(), author.getMemberId(), ApplicantStatus.APPROVED)).willReturn(false);
        given(applicantRepository.findByProjectProjectId(project.getProjectId())).willReturn(List.of(applicant));

        // when
        List<ApplicantResponse> responses = applicantService.getApplicants(author.getMemberId(), project.getProjectId());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(ApplicantStatus.PENDING);
    }

    @Test
    @DisplayName("승인된 참여자도 지원자 목록을 조회할 수 있다")
    void getApplicants_byApprovedMember_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member approvedMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);
        Applicant applicant = buildApplicant(1L, project, approvedMember);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                project.getProjectId(), approvedMember.getMemberId(), ApplicantStatus.APPROVED)).willReturn(true);
        given(applicantRepository.findByProjectProjectId(project.getProjectId())).willReturn(List.of(applicant));

        // when
        List<ApplicantResponse> responses = applicantService.getApplicants(approvedMember.getMemberId(), project.getProjectId());

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("작성자도 승인된 참여자도 아닌 경우 FORBIDDEN 예외를 던진다")
    void getApplicants_unauthorized_throwsForbidden() {
        // given
        Member author = MemberFixture.developer(1L);
        Member stranger = MemberFixture.designer(3L);
        Project project = ProjectFixture.recruiting(author);

        given(projectRepository.findById(project.getProjectId())).willReturn(Optional.of(project));
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                project.getProjectId(), stranger.getMemberId(), ApplicantStatus.APPROVED)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> applicantService.getApplicants(stranger.getMemberId(), project.getProjectId()))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ================================
    // updateStatus
    // ================================

    @Test
    @DisplayName("작성자가 지원자를 승인하면 APPROVED 상태로 변경하고 알림을 발행한다")
    void updateStatus_approve_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member applicantMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);
        Applicant applicant = buildApplicant(1L, project, applicantMember);
        UpdateApplicantStatusRequest request = new UpdateApplicantStatusRequest(ApplicantStatus.APPROVED);

        given(applicantRepository.findById(applicant.getApplicantId())).willReturn(Optional.of(applicant));

        // when
        ApplicantResponse response = applicantService.updateStatus(
                author.getMemberId(), applicant.getApplicantId(), request);

        // then
        assertThat(response.status()).isEqualTo(ApplicantStatus.APPROVED);
        then(eventPublisher).should().publishEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("작성자가 아닌 경우 상태 변경 시 FORBIDDEN 예외를 던진다")
    void updateStatus_notAuthor_throwsForbidden() {
        // given
        Member author = MemberFixture.developer(1L);
        Member stranger = MemberFixture.designer(3L);
        Member applicantMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);
        Applicant applicant = buildApplicant(1L, project, applicantMember);
        UpdateApplicantStatusRequest request = new UpdateApplicantStatusRequest(ApplicantStatus.APPROVED);

        given(applicantRepository.findById(applicant.getApplicantId())).willReturn(Optional.of(applicant));

        // when & then
        assertThatThrownBy(() -> applicantService.updateStatus(
                stranger.getMemberId(), applicant.getApplicantId(), request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 지원자 ID로 상태 변경 시 APPLICANT_NOT_FOUND 예외를 던진다")
    void updateStatus_applicantNotFound_throwsException() {
        // given
        UpdateApplicantStatusRequest request = new UpdateApplicantStatusRequest(ApplicantStatus.APPROVED);
        given(applicantRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicantService.updateStatus(1L, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.APPLICANT_NOT_FOUND));
    }

    // ================================
    // getJoinedProjects
    // ================================

    @Test
    @DisplayName("승인된 프로젝트 목록 조회 시 APPROVED 상태의 프로젝트만 반환한다")
    void getJoinedProjects_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member joinedMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(author);
        Applicant approvedApplicant = buildApplicant(1L, project, joinedMember);
        approvedApplicant.updateStatus(ApplicantStatus.APPROVED);

        given(applicantRepository.findByMemberMemberIdAndStatus(joinedMember.getMemberId(), ApplicantStatus.APPROVED))
                .willReturn(List.of(approvedApplicant));

        // when
        var responses = applicantService.getJoinedProjects(joinedMember.getMemberId());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(project.getProjectId());
    }

    // ================================
    // helpers
    // ================================

    private Applicant buildApplicant(Long applicantId, Project project, Member member) {
        Applicant applicant = Applicant.builder()
                .project(project)
                .member(member)
                .build();
        ReflectionTestUtils.setField(applicant, "applicantId", applicantId);
        return applicant;
    }
}
