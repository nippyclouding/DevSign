-- ================================================================
-- DevSign Connect - PostgreSQL DDL
-- DB: PostgreSQL
-- ================================================================
drop database devsign;
create database devsign;
-- ----------------------------------------------------------------
-- 1. MEMBER (회원)
-- ----------------------------------------------------------------
CREATE TABLE member (
    member_id   BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(100)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    section     VARCHAR(20)     NOT NULL CHECK (section IN ('DEVELOPER', 'DESIGNER')),
    reputation  INTEGER         NOT NULL DEFAULT 0,
    profile_data TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- 2. PROJECT (프로젝트 모집글)
--    member_id: 작성자
--    status: RECRUITING(모집중) / PROGRESS(진행중) / COMPLETED(완료)
-- ----------------------------------------------------------------
CREATE TABLE project (
    project_id          BIGSERIAL       PRIMARY KEY,
    member_id           BIGINT          NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    main_title          VARCHAR(100)    NOT NULL,
    subtitle            VARCHAR(200),
    content             TEXT            NOT NULL,
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'RECRUITING'
                            CHECK (status IN ('RECRUITING', 'PROGRESS', 'COMPLETED')),
    needed_developers   INTEGER         NOT NULL DEFAULT 0,
    needed_designers    INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- 3. GROUP_CHAT (그룹채팅방)
--    PROJECT와 1:1 식별관계
--    프로젝트 생성 시 함께 생성
-- ----------------------------------------------------------------
CREATE TABLE group_chat (
    group_chat_id   BIGSERIAL   PRIMARY KEY,
    project_id      BIGINT      NOT NULL UNIQUE REFERENCES project(project_id) ON DELETE CASCADE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- 4. APPLICANT (지원 / 참여자)
--    PENDING  : 지원 대기
--    APPROVED : 승인 = 프로젝트 참여자
--    REJECTED : 거절
--    참여자 조회: WHERE status = 'APPROVED'
--    중복 지원 방지: UNIQUE(project_id, member_id)
-- ----------------------------------------------------------------
CREATE TABLE applicant (
    applicant_id    BIGSERIAL   PRIMARY KEY,
    project_id      BIGINT      NOT NULL REFERENCES project(project_id) ON DELETE CASCADE,
    member_id       BIGINT      NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, member_id)
);

-- ----------------------------------------------------------------
-- 5. MESSAGE (채팅 메시지)
--    group_chat_id : 어느 채팅방
--    member_id     : 보낸 사람
-- ----------------------------------------------------------------
CREATE TABLE message (
    message_id      BIGSERIAL   PRIMARY KEY,
    group_chat_id   BIGINT      NOT NULL REFERENCES group_chat(group_chat_id) ON DELETE CASCADE,
    member_id       BIGINT      NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    content         TEXT        NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- 6. REVIEW (리뷰)
--    완료된 프로젝트의 팀원 리뷰
--    중복 리뷰 방지: UNIQUE(project_id, reviewer_id, reviewee_id)
-- ----------------------------------------------------------------
CREATE TABLE review (
    review_id       BIGSERIAL   PRIMARY KEY,
    project_id      BIGINT      NOT NULL REFERENCES project(project_id) ON DELETE CASCADE,
    reviewer_id     BIGINT      NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    reviewee_id     BIGINT      NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    content         TEXT        NOT NULL,
    rating          INTEGER     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, reviewer_id, reviewee_id)
);

-- ----------------------------------------------------------------
-- 7. NOTIFICATION (알림)
--    type: NEW_APPLICANT / APPROVED / REJECTED / REVIEW_REQUEST
--    related_id: 관련 프로젝트 or 지원 ID
-- ----------------------------------------------------------------
CREATE TABLE notification (
    notification_id BIGSERIAL       PRIMARY KEY,
    member_id       BIGINT          NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    type            VARCHAR(50)     NOT NULL
                        CHECK (type IN ('NEW_APPLICANT', 'APPROVED', 'REJECTED', 'REVIEW_REQUEST')),
    message         TEXT            NOT NULL,
    related_id      BIGINT,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ================================================================
-- INDEX
-- ================================================================

-- member
CREATE INDEX idx_member_email ON member(email);

-- project
CREATE INDEX idx_project_member_id  ON project(member_id);
CREATE INDEX idx_project_status     ON project(status);

-- applicant
CREATE INDEX idx_applicant_project_id   ON applicant(project_id);
CREATE INDEX idx_applicant_member_id    ON applicant(member_id);
CREATE INDEX idx_applicant_status       ON applicant(project_id, status);

-- message
CREATE INDEX idx_message_group_chat_id  ON message(group_chat_id);
CREATE INDEX idx_message_created_at     ON message(group_chat_id, created_at);

-- review
CREATE INDEX idx_review_reviewee_id     ON review(reviewee_id);
CREATE INDEX idx_review_project_id      ON review(project_id);

-- notification
CREATE INDEX idx_notification_member_id ON notification(member_id, is_read);
