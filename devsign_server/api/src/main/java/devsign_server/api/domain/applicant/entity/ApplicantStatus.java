package devsign_server.api.domain.applicant.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicantStatus {
    PENDING, APPROVED, REJECTED;

    @JsonValue
    public String toLower() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ApplicantStatus from(String value) {
        return switch (value.toUpperCase()) {
            case "PENDING" -> PENDING;
            case "APPROVED" -> APPROVED;
            case "REJECTED" -> REJECTED;
            default -> throw new IllegalArgumentException("Unknown status: " + value);
        };
    }
}
