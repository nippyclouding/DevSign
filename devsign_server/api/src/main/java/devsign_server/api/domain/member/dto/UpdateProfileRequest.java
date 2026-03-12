package devsign_server.api.domain.member.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
        String name,

        @Size(max = 2000, message = "프로필은 2000자 이하로 입력해주세요.")
        String profileData
) {}
