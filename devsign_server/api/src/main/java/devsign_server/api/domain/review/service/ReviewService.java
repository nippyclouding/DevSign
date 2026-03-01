package devsign_server.api.domain.review.service;

import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.applicant.repository.ApplicantRepository;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import devsign_server.api.domain.project.repository.ProjectRepository;
import devsign_server.api.domain.review.dto.CreateReviewRequest;
import devsign_server.api.domain.review.dto.ReviewResponse;
import devsign_server.api.domain.review.entity.Review;
import devsign_server.api.domain.review.repository.ReviewRepository;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final ApplicantRepository applicantRepository;

    @Transactional
    public ReviewResponse createReview(Long reviewerId, CreateReviewRequest request) {
        Project project = projectRepository.findById(request.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new CustomException(ErrorCode.PROJECT_NOT_COMPLETED);
        }

        boolean isReviewerMember = project.isAuthor(reviewerId) ||
                applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                        request.postId(), reviewerId, ApplicantStatus.APPROVED);
        if (!isReviewerMember) {
            throw new CustomException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        boolean isRevieweeMember = project.isAuthor(request.revieweeId()) ||
                applicantRepository.existsByProjectProjectIdAndMemberMemberIdAndStatus(
                        request.postId(), request.revieweeId(), ApplicantStatus.APPROVED);
        if (!isRevieweeMember) {
            throw new CustomException(ErrorCode.NOT_PROJECT_MEMBER);
        }

        if (reviewRepository.existsByProjectProjectIdAndReviewerMemberIdAndRevieweeMemberId(
                request.postId(), reviewerId, request.revieweeId())) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        Member reviewer = memberRepository.findById(reviewerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member reviewee = memberRepository.findById(request.revieweeId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = Review.builder()
                .project(project)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .content(request.content())
                .rating(request.rating())
                .build();

        reviewRepository.save(review);
        reviewee.addReputation(request.rating());

        return ReviewResponse.from(review);
    }

    public List<ReviewResponse> getReviewsByReviewee(Long revieweeId) {
        return reviewRepository.findByRevieweeMemberIdOrderByCreatedAtDesc(revieweeId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
