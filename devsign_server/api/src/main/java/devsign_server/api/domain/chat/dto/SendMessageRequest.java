package devsign_server.api.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotBlank(message = "메시지를 입력해주세요.")
        @Size(max = 1000, message = "메시지는 1000자 이하로 입력해주세요.")
        String content
) {}
