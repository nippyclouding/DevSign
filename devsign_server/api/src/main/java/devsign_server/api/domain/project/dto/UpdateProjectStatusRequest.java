package devsign_server.api.domain.project.dto;

import devsign_server.api.domain.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProjectStatusRequest(

        @NotNull(message = "상태를 선택해주세요.")
        ProjectStatus status
) {}
