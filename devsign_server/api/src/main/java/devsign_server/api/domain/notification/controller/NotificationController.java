package devsign_server.api.domain.notification.controller;

import devsign_server.api.domain.notification.dto.NotificationResponse;
import devsign_server.api.domain.notification.service.NotificationService;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return ApiResponse.ok(notificationService.getNotifications(memberDetails.getMemberId()));
    }

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(memberDetails.getMemberId(), notificationId);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal MemberDetails memberDetails) {
        notificationService.markAllAsRead(memberDetails.getMemberId());
    }
}
