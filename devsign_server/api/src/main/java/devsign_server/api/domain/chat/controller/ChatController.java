package devsign_server.api.domain.chat.controller;

import devsign_server.api.domain.chat.dto.MessageResponse;
import devsign_server.api.domain.chat.service.ChatService;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{projectId}/messages")
    public ApiResponse<List<MessageResponse>> getMessages(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long projectId
    ) {
        return ApiResponse.ok(chatService.getMessages(memberDetails.getMemberId(), projectId));
    }
}
