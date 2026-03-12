package devsign_server.api.domain.project.controller;

import devsign_server.api.domain.project.dto.*;
import devsign_server.api.domain.project.entity.ProjectStatus;
import devsign_server.api.domain.project.service.ProjectService;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<Page<ProjectSummaryResponse>> getProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(projectService.getProjects(status, section, keyword, pageable));
    }

    @GetMapping("/stats")
    public ApiResponse<ProjectStatsResponse> getStats() {
        return ApiResponse.ok(projectService.getStats());
    }

    @GetMapping("/me")
    public ApiResponse<List<ProjectSummaryResponse>> getMyProjects(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return ApiResponse.ok(projectService.getMyProjects(memberDetails.getMemberId()));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProject(@PathVariable Long projectId) {
        return ApiResponse.ok(projectService.getProject(projectId));
    }

    @GetMapping("/{projectId}/membership")
    public ApiResponse<MembershipResponse> getMembership(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId
    ) {
        return ApiResponse.ok(projectService.getMembership(memberDetails.getMemberId(), projectId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectDetailResponse> createProject(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return ApiResponse.ok(projectService.createProject(memberDetails.getMemberId(), request));
    }

    @PutMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> updateProject(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ApiResponse.ok(projectService.updateProject(memberDetails.getMemberId(), projectId, request));
    }

    @PatchMapping("/{projectId}/status")
    public ApiResponse<ProjectDetailResponse> updateStatus(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectStatusRequest request
    ) {
        return ApiResponse.ok(projectService.updateStatus(memberDetails.getMemberId(), projectId, request));
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId
    ) {
        projectService.deleteProject(memberDetails.getMemberId(), projectId);
    }
}
