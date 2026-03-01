package devsign_server.api.domain.chat.dto;

import devsign_server.api.domain.chat.entity.Message;
import devsign_server.api.domain.member.entity.Section;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long userId,
        String userName,
        Section userRole,
        String content,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getMessageId(),
                message.getMember().getMemberId(),
                message.getMember().getName(),
                message.getMember().getSection(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
