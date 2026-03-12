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
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "허용되지 않는 상태 전환입니다."),

    // Applicant
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "이미 지원한 프로젝트입니다."),
    SELF_APPLICATION(HttpStatus.BAD_REQUEST, "본인의 프로젝트에는 지원할 수 없습니다."),
    APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "지원 정보를 찾을 수 없습니다."),
    NOT_APPROVED_MEMBER(HttpStatus.FORBIDDEN, "승인된 참여자만 접근할 수 있습니다."),
    PROJECT_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "모집 중인 프로젝트에만 지원할 수 있습니다."),
    ROLE_MISMATCH(HttpStatus.BAD_REQUEST, "프로젝트에서 해당 직군을 모집하지 않습니다."),
    APPLICANT_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "해당 직군의 모집 인원이 마감되었습니다."),
    APPLICATION_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "대기 중인 지원만 취소할 수 있습니다."),

    // Review
    ALREADY_REVIEWED(HttpStatus.CONFLICT, "이미 리뷰를 작성했습니다."),
    PROJECT_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 프로젝트에만 리뷰를 작성할 수 있습니다."),
    NOT_PROJECT_MEMBER(HttpStatus.FORBIDDEN, "프로젝트 참여자만 리뷰를 작성할 수 있습니다."),
    SELF_REVIEW(HttpStatus.BAD_REQUEST, "본인에게 리뷰를 작성할 수 없습니다."),

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
