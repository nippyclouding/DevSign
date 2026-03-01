package devsign_server.api.domain.member.controller;

import devsign_server.api.domain.applicant.service.ApplicantService;
import devsign_server.api.domain.member.dto.MemberResponse;
import devsign_server.api.domain.member.dto.MyProjectsResponse;
import devsign_server.api.domain.member.dto.UpdateProfileRequest;
import devsign_server.api.domain.member.service.MemberService;
import devsign_server.api.domain.project.dto.ProjectSummaryResponse;
import devsign_server.api.domain.project.service.ProjectService;
import devsign_server.api.domain.review.dto.ReviewResponse;
import devsign_server.api.domain.review.service.ReviewService;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ReviewService reviewService;
    private final ProjectService projectService;
    private final ApplicantService applicantService;

    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getProfile(@PathVariable Long memberId) {
        return ApiResponse.ok(memberService.getProfile(memberId));
    }

    @GetMapping("/{memberId}/reviews")
    public ApiResponse<List<ReviewResponse>> getReviews(@PathVariable Long memberId) {
        return ApiResponse.ok(reviewService.getReviewsByReviewee(memberId));
    }

    @PutMapping("/me")
    public ApiResponse<MemberResponse> updateProfile(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.ok(memberService.updateProfile(memberDetails.getMemberId(), request));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal MemberDetails memberDetails) {
        memberService.deleteAccount(memberDetails.getMemberId());
    }

    @GetMapping("/me/projects")
    public ApiResponse<MyProjectsResponse> getMyProjects(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getMemberId();
        List<ProjectSummaryResponse> created = projectService.getMyProjects(memberId);
        List<ProjectSummaryResponse> joined = applicantService.getJoinedProjects(memberId);
        return ApiResponse.ok(new MyProjectsResponse(created, joined));
    }
}
