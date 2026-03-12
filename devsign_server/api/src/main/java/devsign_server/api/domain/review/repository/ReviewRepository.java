package devsign_server.api.domain.review.repository;

import devsign_server.api.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProjectProjectIdAndReviewerMemberIdAndRevieweeMemberId(
            Long projectId, Long reviewerId, Long revieweeId);

    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer JOIN FETCH r.reviewee JOIN FETCH r.project WHERE r.reviewee.memberId = :revieweeId ORDER BY r.createdAt DESC")
    List<Review> findByRevieweeMemberIdOrderByCreatedAtDesc(@Param("revieweeId") Long revieweeId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.reviewer.memberId = :memberId OR r.reviewee.memberId = :memberId")
    void deleteByReviewerOrReviewee(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
