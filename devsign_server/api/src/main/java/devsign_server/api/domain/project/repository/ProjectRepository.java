package devsign_server.api.domain.project.repository;

import devsign_server.api.domain.member.entity.Section;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            SELECT p FROM Project p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:section IS NULL OR
                (:section = 'DEVELOPER' AND p.neededDevelopers > 0) OR
                (:section = 'DESIGNER' AND p.neededDesigners > 0))
            AND (:keyword IS NULL OR
                p.mainTitle LIKE %:keyword% OR
                p.subtitle LIKE %:keyword% OR
                p.content LIKE %:keyword%)
            ORDER BY p.createdAt DESC
            """)
    Page<Project> findAllWithFilter(
            @Param("status") ProjectStatus status,
            @Param("section") String section,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Project> findByMemberMemberIdOrderByCreatedAtDesc(Long memberId);
}
