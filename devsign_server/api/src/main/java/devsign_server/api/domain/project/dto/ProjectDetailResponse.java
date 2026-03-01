package devsign_server.api.domain.project.dto;

import devsign_server.api.domain.member.entity.Section;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.domain.project.entity.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectDetailResponse(
        Long id,
        Long authorId,
        String authorName,
        Section authorRole,
        int authorReputation,
        String mainTitle,
        String subtitle,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status,
        int neededDevelopers,
        int neededDesigners,
        Long groupChatId,
        LocalDateTime createdAt
) {
    public static ProjectDetailResponse from(Project project, Long groupChatId) {
        return new ProjectDetailResponse(
                project.getProjectId(),
                project.getMember().getMemberId(),
                project.getMember().getName(),
                project.getMember().getSection(),
                project.getMember().getReputation(),
                project.getMainTitle(),
                project.getSubtitle(),
                project.getContent(),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus(),
                project.getNeededDevelopers(),
                project.getNeededDesigners(),
                groupChatId,
                project.getCreatedAt()
        );
    }
}
