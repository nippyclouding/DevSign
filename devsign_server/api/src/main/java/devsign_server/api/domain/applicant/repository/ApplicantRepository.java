package devsign_server.api.domain.applicant.repository;

import devsign_server.api.domain.applicant.entity.Applicant;
import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    boolean existsByProjectProjectIdAndMemberMemberId(Long projectId, Long memberId);

    Optional<Applicant> findByProjectProjectIdAndMemberMemberId(Long projectId, Long memberId);

    @Query("SELECT a FROM Applicant a JOIN FETCH a.member WHERE a.project.projectId = :projectId")
    List<Applicant> findByProjectProjectId(@Param("projectId") Long projectId);

    boolean existsByProjectProjectIdAndMemberMemberIdAndStatus(Long projectId, Long memberId, ApplicantStatus status);

    @Query("SELECT a FROM Applicant a JOIN FETCH a.project p JOIN FETCH p.member WHERE a.member.memberId = :memberId AND a.status = :status")
    List<Applicant> findByMemberMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") ApplicantStatus status);

    @Modifying
    @Query("DELETE FROM Applicant a WHERE a.member.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Applicant a WHERE a.project.member.memberId = :memberId")
    void deleteByProjectAuthorMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Applicant a WHERE a.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(a) FROM Applicant a WHERE a.project.projectId = :projectId AND a.member.section = :section AND a.status = devsign_server.api.domain.applicant.entity.ApplicantStatus.APPROVED")
    long countApprovedByProjectAndSection(@Param("projectId") Long projectId, @Param("section") devsign_server.api.domain.member.entity.Section section);
}
