package devsign_server.api.domain.project.dto;

public record MembershipResponse(boolean isAuthor, boolean isApproved, String applicationStatus, Long applicationId) {}
