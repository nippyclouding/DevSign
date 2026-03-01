package devsign_server.api.domain.project.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
    RECRUITING, PROGRESS, COMPLETED;

    @JsonValue
    public String toLower() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ProjectStatus from(String value) {
        return switch (value.toUpperCase()) {
            case "RECRUITING" -> RECRUITING;
            case "PROGRESS" -> PROGRESS;
            case "COMPLETED" -> COMPLETED;
            default -> throw new IllegalArgumentException("Unknown status: " + value);
        };
    }
}
