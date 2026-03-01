package devsign_server.api.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Section {
    DEVELOPER, DESIGNER;

    @JsonValue
    public String toLower() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Section from(String value) {
        return switch (value.toUpperCase()) {
            case "DEVELOPER" -> DEVELOPER;
            case "DESIGNER" -> DESIGNER;
            default -> throw new IllegalArgumentException("Unknown section: " + value);
        };
    }
}
