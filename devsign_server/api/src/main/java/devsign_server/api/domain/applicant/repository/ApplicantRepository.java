package devsign_server.api.domain.applicant.repository;

import devsign_server.api.domain.applicant.entity.Applicant;
import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    boolean existsByProjectProjectIdAndMemberMemberId(Long projectId, Long memberId);

    Optional<Applicant> findByProjectProjectIdAndMemberMemberId(Long projectId, Long memberId);

    List<Applicant> findByProjectProjectId(Long projectId);

    boolean existsByProjectProjectIdAndMemberMemberIdAndStatus(Long projectId, Long memberId, ApplicantStatus status);

    List<Applicant> findByMemberMemberIdAndStatus(Long memberId, ApplicantStatus status);
}
