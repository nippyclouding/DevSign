package devsign_server.api.domain.applicant.dto;

import devsign_server.api.domain.applicant.entity.Applicant;
import devsign_server.api.domain.applicant.entity.ApplicantStatus;
import devsign_server.api.domain.member.entity.Section;

import java.time.LocalDateTime;

public record ApplicantResponse(
        Long id,
        Long postId,
        Long applicantId,
        String applicantName,
        Section applicantRole,
        int applicantReputation,
        String applicantProfile,
        ApplicantStatus status,
        LocalDateTime createdAt
) {
    public static ApplicantResponse from(Applicant applicant) {
        return new ApplicantResponse(
                applicant.getApplicantId(),
                applicant.getProject().getProjectId(),
                applicant.getMember().getMemberId(),
                applicant.getMember().getName(),
                applicant.getMember().getSection(),
                applicant.getMember().getReputation(),
                applicant.getMember().getProfileData(),
                applicant.getStatus(),
                applicant.getCreatedAt()
        );
    }
}
