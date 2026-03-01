package devsign_server.api.domain.review.dto;

import devsign_server.api.domain.review.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long postId,
        String projectTitle,
        Long reviewerId,
        String reviewerName,
        Long revieweeId,
        String content,
        int rating,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getProject().getProjectId(),
                review.getProject().getMainTitle(),
                review.getReviewer().getMemberId(),
                review.getReviewer().getName(),
                review.getReviewee().getMemberId(),
                review.getContent(),
                review.getRating(),
                review.getCreatedAt()
        );
    }
}
