package devsign_server.api.domain.member.dto;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.entity.Section;

public record MemberResponse(
        Long id,
        String email,
        String name,
        Section role,
        double reputation,
        String profileData
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getSection(),
                member.getReputation(),
                member.getProfileData()
        );
    }
}
