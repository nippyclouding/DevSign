package devsign_server.api.global.auth.dto;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.entity.Section;

public record LoginResponse(
        Long id,
        String email,
        String name,
        Section role,
        int reputation,
        String profileData,
        String token
) {
    public static LoginResponse of(Member member, String token) {
        return new LoginResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getSection(),
                member.getReputation(),
                member.getProfileData(),
                token
        );
    }
}
