package devsign_server.api.domain.applicant.controller;

import devsign_server.api.domain.applicant.dto.ApplicantResponse;
import devsign_server.api.domain.applicant.dto.UpdateApplicantStatusRequest;
import devsign_server.api.domain.applicant.service.ApplicantService;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApplicantController {

    private final ApplicantService applicantService;

    @PostMapping("/api/projects/{projectId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ApplicantResponse> apply(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId
    ) {
        return ApiResponse.ok(applicantService.apply(memberDetails.getMemberId(), projectId));
    }

    @GetMapping("/api/projects/{projectId}/applicants")
    public ApiResponse<List<ApplicantResponse>> getApplicants(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId
    ) {
        return ApiResponse.ok(applicantService.getApplicants(memberDetails.getMemberId(), projectId));
    }

    @PatchMapping("/api/applicants/{applicantId}/status")
    public ApiResponse<ApplicantResponse> updateStatus(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long applicantId,
            @Valid @RequestBody UpdateApplicantStatusRequest request
    ) {
        return ApiResponse.ok(applicantService.updateStatus(memberDetails.getMemberId(), applicantId, request));
    }

    @DeleteMapping("/api/applicants/{applicantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelApplication(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long applicantId
    ) {
        applicantService.cancelApplication(memberDetails.getMemberId(), applicantId);
    }
}
