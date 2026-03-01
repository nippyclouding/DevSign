package devsign_server.api.domain.member.dto;

import devsign_server.api.domain.project.dto.ProjectSummaryResponse;

import java.util.List;

public record MyProjectsResponse(
        List<ProjectSummaryResponse> created,
        List<ProjectSummaryResponse> joined
) {}
