package devsign_server.api.fixture;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

/**
 * 테스트에서 공통으로 사용하는 Project 객체 생성 도우미.
 */
public final class ProjectFixture {

    private ProjectFixture() {}

    public static Project recruiting(Member author) {
        return create(1L, author, ProjectStatus.RECRUITING);
    }

    public static Project recruiting(Long projectId, Member author) {
        return create(projectId, author, ProjectStatus.RECRUITING);
    }

    public static Project completed(Long projectId, Member author) {
        return create(projectId, author, ProjectStatus.COMPLETED);
    }

    public static Project create_with_status(Long projectId, Member author, ProjectStatus status) {
        return create(projectId, author, status);
    }

    private static Project create(Long projectId, Member author, ProjectStatus status) {
        Project project = Project.builder()
                .member(author)
                .mainTitle("테스트 프로젝트")
                .subtitle("서브 타이틀입니다")
                .content("프로젝트 상세 내용입니다")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .neededDevelopers(2)
                .neededDesigners(1)
                .build();
        project.updateStatus(status);
        ReflectionTestUtils.setField(project, "projectId", projectId);
        return project;
    }
}
