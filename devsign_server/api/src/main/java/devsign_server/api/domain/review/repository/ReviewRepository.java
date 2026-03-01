package devsign_server.api.domain.review.repository;

import devsign_server.api.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProjectProjectIdAndReviewerMemberIdAndRevieweeMemberId(
            Long projectId, Long reviewerId, Long revieweeId);

    List<Review> findByRevieweeMemberIdOrderByCreatedAtDesc(Long revieweeId);
}
