package devsign_server.api.domain.applicant.dto;

import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicantStatusRequest(

        @NotNull(message = "상태를 선택해주세요.")
        ApplicantStatus status
) {}
