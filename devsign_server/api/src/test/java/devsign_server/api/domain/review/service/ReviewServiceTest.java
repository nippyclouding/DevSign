package devsign_server.api.domain.review.service;

import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.domain.review.dto.CreateReviewRequest;
import devsign_server.api.domain.review.dto.ReviewResponse;
import devsign_server.api.domain.review.entity.Review;
import devsign_server.api.domain.review.repository.ReviewRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DisplayName("ReviewService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicantRepository applicantRepository;

    // ================================
    // createReview
    // ================================

    @Test
    @DisplayName("완료된 프로젝트에서 참여자가 리뷰 작성 성공 시 ReviewResponse를 반환하고 평판이 누적된다")
    void createReview_byApprovedMember_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member revieweeMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.completed(10L, author);
        CreateReviewRequest request = new CreateReviewRequest(project.getProjectId(), revieweeMember.getMemberId(), "훌륭한 협업이었습니다.", 5);

        given(projectRepository.findById(request.postId())).willReturn(Optional.of(project));
        // 리뷰어(author)는 project.isAuthor()가 true라 applicantRepository를 호출하지 않음 (단락 평가)
        // 피리뷰어(revieweeMember)는 승인된 참여자
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                request.postId(), revieweeMember.getMemberId(), ApplicantStatus.APPROVED)).willReturn(true);
        given(reviewRepository.existsByProjectProjectIdAndReviewerMemberIdAndRevieweeMemberId(
                request.postId(), author.getMemberId(), revieweeMember.getMemberId())).willReturn(false);
        given(memberRepository.findById(author.getMemberId())).willReturn(Optional.of(author));
        given(memberRepository.findById(revieweeMember.getMemberId())).willReturn(Optional.of(revieweeMember));
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            ReflectionTestUtils.setField(review, "reviewId", 1L);
            return review;
        });

        int reputationBefore = revieweeMember.getReputation();

        // when
        ReviewResponse response = reviewService.createReview(author.getMemberId(), request);

        // then
        assertThat(response.rating()).isEqualTo(5);
        assertThat(revieweeMember.getReputation()).isEqualTo(reputationBefore + 5);
    }

    @Test
    @DisplayName("완료되지 않은 프로젝트에 리뷰 작성 시 PROJECT_NOT_COMPLETED 예외를 던진다")
    void createReview_projectNotCompleted_throwsException() {
        // given
        Member author = MemberFixture.developer(1L);
        Member revieweeMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.recruiting(10L, author); // RECRUITING 상태
        CreateReviewRequest request = new CreateReviewRequest(project.getProjectId(), revieweeMember.getMemberId(), "리뷰", 5);

        given(projectRepository.findById(request.postId())).willReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(author.getMemberId(), request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PROJECT_NOT_COMPLETED));
    }

    @Test
    @DisplayName("프로젝트 참여자가 아닌 경우 리뷰 작성 시 NOT_PROJECT_MEMBER 예외를 던진다")
    void createReview_reviewerNotMember_throwsException() {
        // given
        Member author = MemberFixture.developer(1L);
        Member stranger = MemberFixture.designer(3L);
        Member revieweeMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.completed(10L, author);
        CreateReviewRequest request = new CreateReviewRequest(project.getProjectId(), revieweeMember.getMemberId(), "리뷰", 4);

        given(projectRepository.findById(request.postId())).willReturn(Optional.of(project));
        // stranger는 작성자도 아니고 승인된 참여자도 아님
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                request.postId(), stranger.getMemberId(), ApplicantStatus.APPROVED)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(stranger.getMemberId(), request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_PROJECT_MEMBER));
    }

    @Test
    @DisplayName("이미 리뷰를 작성한 경우 ALREADY_REVIEWED 예외를 던진다")
    void createReview_alreadyReviewed_throwsException() {
        // given
        Member author = MemberFixture.developer(1L);
        Member revieweeMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.completed(10L, author);
        CreateReviewRequest request = new CreateReviewRequest(project.getProjectId(), revieweeMember.getMemberId(), "중복 리뷰", 3);

        given(projectRepository.findById(request.postId())).willReturn(Optional.of(project));
        // 리뷰어(author)는 project.isAuthor()가 true라 applicantRepository를 호출하지 않음 (단락 평가)
        // 피리뷰어(revieweeMember)는 승인된 참여자
        given(applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                request.postId(), revieweeMember.getMemberId(), ApplicantStatus.APPROVED)).willReturn(true);
        given(reviewRepository.existsByProjectProjectIdAndReviewerMemberIdAndRevieweeMemberId(
                request.postId(), author.getMemberId(), revieweeMember.getMemberId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(author.getMemberId(), request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_REVIEWED));
    }

    // ================================
    // getReviewsByReviewee
    // ================================

    @Test
    @DisplayName("피리뷰어 ID로 리뷰 목록 조회 시 최신순으로 반환한다")
    void getReviewsByReviewee_success() {
        // given
        Member author = MemberFixture.developer(1L);
        Member revieweeMember = MemberFixture.designer(2L);
        Project project = ProjectFixture.completed(10L, author);

        Review review = Review.builder()
                .project(project)
                .reviewer(author)
                .reviewee(revieweeMember)
                .content("좋았습니다")
                .rating(4)
                .build();
        ReflectionTestUtils.setField(review, "reviewId", 1L);

        given(reviewRepository.findByRevieweeMemberIdOrderByCreatedAtDesc(revieweeMember.getMemberId()))
                .willReturn(List.of(review));

        // when
        List<ReviewResponse> responses = reviewService.getReviewsByReviewee(revieweeMember.getMemberId());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).rating()).isEqualTo(4);
        assertThat(responses.get(0).reviewerId()).isEqualTo(author.getMemberId());
    }
}
