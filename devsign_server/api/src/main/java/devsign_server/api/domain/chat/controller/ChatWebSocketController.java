package devsign_server.api.domain.chat.controller;

import devsign_server.api.domain.chat.dto.MessageResponse;
import devsign_server.api.domain.chat.dto.SendMessageRequest;
import devsign_server.api.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{groupChatId}")
    public void sendMessage(
            @DestinationVariable Long groupChatId,
            @Payload SendMessageRequest request,
            org.springframework.messaging.simp.stomp.StompHeaderAccessor headerAccessor
    ) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            throw new IllegalStateException("세션 정보가 없습니다.");
        }
        Long memberId = (Long) sessionAttributes.get("memberId");
        if (memberId == null) {
            throw new IllegalStateException("인증되지 않은 요청입니다.");
        }

        MessageResponse response = chatService.saveMessage(memberId, groupChatId, request.content());
        messagingTemplate.convertAndSend("/sub/chat/" + groupChatId, response);
    }
}
