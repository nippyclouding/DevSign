package devsign_server.api.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Applicant
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "이미 지원한 프로젝트입니다."),
    SELF_APPLICATION(HttpStatus.BAD_REQUEST, "본인의 프로젝트에는 지원할 수 없습니다."),
    APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "지원 정보를 찾을 수 없습니다."),
    NOT_APPROVED_MEMBER(HttpStatus.FORBIDDEN, "승인된 참여자만 접근할 수 있습니다."),

    // Review
    ALREADY_REVIEWED(HttpStatus.CONFLICT, "이미 리뷰를 작성했습니다."),
    PROJECT_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 프로젝트에만 리뷰를 작성할 수 있습니다."),
    NOT_PROJECT_MEMBER(HttpStatus.FORBIDDEN, "프로젝트 참여자만 리뷰를 작성할 수 있습니다."),

    // Chat
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
