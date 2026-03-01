package devsign_server.api.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(

        @NotBlank(message = "메시지를 입력해주세요.")
        String content
) {}
