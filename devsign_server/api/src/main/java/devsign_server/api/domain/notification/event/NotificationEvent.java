package devsign_server.api.domain.notification.event;

import devsign_server.api.domain.notification.entity.NotificationType;
import lombok.Getter;

@Getter
public class NotificationEvent {

    private final Long receiverId;
    private final NotificationType type;
    private final String message;
    private final Long relatedId;

    public NotificationEvent(Long receiverId, NotificationType type, String message, Long relatedId) {
        this.receiverId = receiverId;
        this.type = type;
        this.message = message;
        this.relatedId = relatedId;
    }
}
