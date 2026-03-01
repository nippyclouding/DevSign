package devsign_server.api.domain.project.dto;

import devsign_server.api.domain.member.entity.Section;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectSummaryResponse(
        Long id,
        Long authorId,
        String authorName,
        Section authorRole,
        String mainTitle,
        String subtitle,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        int neededDevelopers,
        int neededDesigners,
        LocalDateTime createdAt
) {
    public static ProjectSummaryResponse from(Project project) {
        return new ProjectSummaryResponse(
                project.getProjectId(),
                project.getMember().getMemberId(),
                project.getMember().getName(),
                project.getMember().getSection(),
                project.getMainTitle(),
                project.getSubtitle(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getNeededDevelopers(),
                project.getNeededDesigners(),
                project.getCreatedAt()
        );
    }
}
