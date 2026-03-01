# DevSign Connect

개발자와 디자이너가 만나 포트폴리오 프로젝트·공모전을 함께 진행하는 협업 커뮤니티 플랫폼.

- 프로젝트를 올리고 팀원을 모집할 수 있다
- 개발자 ↔ 디자이너가 역할을 선택해 지원하고 협업한다
- 완료된 프로젝트에서 팀원을 리뷰하고 평판을 쌓는다
- 승인된 멤버끼리 실시간 그룹 채팅으로 소통한다

---

## 모노레포 구조

```
Devsign/
├── devsign_client/          # React 19 + TypeScript 프론트엔드 (현재 Express 임시 서버 포함)
└── devsign_server/          # Spring Boot 4 + JPA 백엔드 (신규 개발 대상)
    └── api/
        ├── build.gradle
        └── src/main/java/devsign_server/api/
            └── member/      # (현재 비어있음)
```

---

## 기술 스택

### Frontend — `devsign_client`

| 영역 | 기술 |
|------|------|
| UI | React 19, TypeScript, Vite 6 |
| 스타일 | Tailwind CSS v4 |
| 애니메이션 | Motion |
| 아이콘 | lucide-react |
| 날짜 | date-fns |

### Backend — `devsign_server` (개발 대상)

| 영역 | 기술 |
|------|------|
| 프레임워크 | Spring Boot 4.0.3 |
| ORM | Spring Data JPA (Hibernate) |
| DB | H2 (개발) → MySQL / PostgreSQL (프로덕션) |
| 보안 | Spring Security + JWT |
| 실시간 | Spring WebSocket (STOMP) |
| 빌드 | Gradle, Java 17 |
| AI | Google Gemini API |

---

## 현재 상태

### devsign_client (프론트엔드)
React 기반 UI가 완성되어 있고, 임시 Express + SQLite 서버에 연결되어 동작 중이다.
**Spring Boot 서버 개발 완료 후 API 연결 대상을 교체**한다.

구현된 UI 페이지:
- `MainPage` — 프로젝트 피드, 상태 필터, 페이지네이션
- `AuthPage` — 회원가입 / 로그인
- `MyPage` — 내 프로필, 참여 프로젝트, 받은 리뷰
- `CreatePostPage` — 프로젝트 모집 글 작성
- `ChatPage` — 실시간 그룹 채팅

### devsign_server (백엔드)
Spring Boot 스켈레톤만 존재하며 **비즈니스 로직이 전무**하다.
아래 명세된 기능을 처음부터 개발해야 한다.

---

## 개발할 기능 명세

---

### 1. 도메인 모델 & DB 설계

JPA Entity 및 테이블 설계. 모든 기능의 기반이 된다.

**Entity 목록:**

```
Member      — 회원 (id, email, password, name, role, reputation, profileData)
Post        — 프로젝트 모집글 (id, author, mainTitle, subtitle, startDate, endDate, content, status, neededDevelopers, neededDesigners)
Application — 지원 (id, post, applicant, status)
Message     — 채팅 메시지 (id, post, sender, content, createdAt)
Review      — 리뷰 (id, post, reviewer, reviewee, content, rating)
Notification— 알림 (id, receiver, type, message, relatedId, isRead, createdAt)
```

**구현 범위:**
- 각 Entity 클래스 작성 (`@Entity`, `@Table`, 연관관계 매핑)
- `application.properties` DB 및 JPA 설정
- H2 콘솔 활성화 (개발 환경)
- Enum 타입: `Role(DEVELOPER, DESIGNER)`, `PostStatus(RECRUITING, PROGRESS, COMPLETED)`, `ApplicationStatus(PENDING, APPROVED, REJECTED)`

---

### 2. 인증 (Auth)

Spring Security + JWT 기반 인증 시스템.

**API:**

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → Access Token 발급 |
| GET | `/api/auth/me` | 내 정보 조회 (인증 필요) |

**구현 범위:**
- `SecurityConfig` — Spring Security 필터 체인, CORS, CSRF 비활성화
- `JwtProvider` — 토큰 생성 / 검증 / 파싱
- `JwtAuthenticationFilter` — 요청마다 토큰 검사
- `MemberDetailsService` — `UserDetailsService` 구현
- 비밀번호 BCrypt 해싱
- `AuthController`, `AuthService`
- 요청/응답 DTO: `SignupRequest`, `LoginRequest`, `LoginResponse`, `MemberResponse`

---

### 3. 회원 (Member)

프로필 조회 및 수정, 계정 삭제, 내 프로젝트 목록.

**API:**

| Method | Endpoint | 설명 |
|--------|----------|------|
| PUT | `/api/members/me` | 내 프로필 수정 (인증 필요) |
| DELETE | `/api/members/me` | 계정 삭제 (인증 필요) |
| GET | `/api/members/{id}` | 특정 회원 프로필 조회 |
| GET | `/api/members/{id}/reviews` | 특정 회원이 받은 리뷰 목록 |
| GET | `/api/members/me/projects` | 내가 만들거나 참여한 프로젝트 목록 (인증 필요) |

**구현 범위:**
- `MemberController`, `MemberService`, `MemberRepository`
- `UpdateProfileRequest`, `MemberProfileResponse`
- 평판(reputation) 점수: 리뷰 작성 시 자동 반영 로직 포함

---

### 4. 프로젝트 포스트 (Post)

모집글 CRUD 및 상태 관리.

**API:**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/posts` | 목록 조회 (페이지네이션, 필터) |
| POST | `/api/posts` | 모집글 작성 (인증 필요) |
| GET | `/api/posts/{id}` | 상세 조회 |
| PATCH | `/api/posts/{id}/status` | 상태 변경 (작성자만) |

**구현 범위:**
- `PostController`, `PostService`, `PostRepository`
- 페이지네이션: Spring Data `Pageable` 적용 (10개/페이지)
- 필터 쿼리: `status`, `role(DEVELOPER|DESIGNER)`, `keyword` 복합 검색
- `@Query` JPQL 또는 `QueryDSL`로 동적 쿼리 작성
- 작성자 본인 확인 후 상태 변경 허용
- DTO: `CreatePostRequest`, `PostSummaryResponse`, `PostDetailResponse`

---

### 5. 지원 (Application)

프로젝트 지원 신청, 승인/거절.

**API:**

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/posts/{postId}/apply` | 지원 신청 (인증 필요) |
| GET | `/api/posts/{postId}/applications` | 지원자 목록 (작성자 또는 승인 멤버만) |
| PATCH | `/api/applications/{id}/status` | 승인 / 거절 (작성자만) |

**구현 범위:**
- `ApplicationController`, `ApplicationService`, `ApplicationRepository`
- 중복 지원 방지 (같은 포스트에 이미 지원한 경우 예외)
- 본인 프로젝트 지원 방지
- 승인 시 알림 생성 트리거 (7번 알림 기능과 연계)
- DTO: `ApplicationResponse`

---

### 6. 채팅 (Chat)

실시간 그룹 채팅. 승인된 멤버와 작성자만 접근 가능.

**API (HTTP):**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/posts/{postId}/messages` | 채팅 기록 조회 (인증 필요, 멤버만) |

**WebSocket (STOMP):**

| 경로 | 설명 |
|------|------|
| `/ws` | WebSocket 연결 엔드포인트 |
| `/pub/chat/{postId}` | 메시지 발행 |
| `/sub/chat/{postId}` | 메시지 구독 |

**구현 범위:**
- `WebSocketConfig` — STOMP 브로커 설정, `/ws` 엔드포인트 등록
- `ChatController` — `@MessageMapping`으로 메시지 수신 및 브로드캐스트
- `MessageRepository` — 채팅 기록 영구 저장
- WebSocket 연결 시 JWT 토큰 검증 (Handshake Interceptor)
- 접근 권한 검증: 작성자 또는 승인된 지원자만 채팅 참여 가능
- DTO: `SendMessageRequest`, `MessageResponse`

---

### 7. 리뷰 (Review)

완료된 프로젝트의 팀원 리뷰 및 평판 점수 반영.

**API:**

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/reviews` | 리뷰 작성 (인증 필요, 완료 프로젝트만) |

**구현 범위:**
- `ReviewController`, `ReviewService`, `ReviewRepository`
- 작성 조건 검증: 프로젝트 상태 `COMPLETED`, 리뷰어와 리뷰이가 같은 프로젝트 멤버
- 중복 리뷰 방지
- 리뷰 저장 후 `reviewee`의 `reputation` 점수 자동 업데이트
- DTO: `CreateReviewRequest`, `ReviewResponse`

---

### 8. 알림 (Notification)

주요 이벤트 발생 시 실시간 알림.

**API:**

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/notifications` | 내 알림 목록 (인증 필요) |
| PATCH | `/api/notifications/{id}/read` | 읽음 처리 |
| PATCH | `/api/notifications/read-all` | 전체 읽음 처리 |

**알림 트리거:**

| 이벤트 | 알림 수신자 |
|--------|------------|
| 내 포스트에 새 지원자 | 포스트 작성자 |
| 지원 승인됨 | 지원자 |
| 지원 거절됨 | 지원자 |
| 프로젝트 완료 → 리뷰 요청 | 프로젝트 참여자 전원 |

**구현 범위:**
- `NotificationController`, `NotificationService`, `NotificationRepository`
- `NotificationEvent` + Spring `ApplicationEventPublisher` 방식으로 도메인 서비스에서 알림 발행
- WebSocket `/sub/notifications/{memberId}`로 실시간 푸시 (선택)
- DTO: `NotificationResponse`

---

### 9. 검색 & 필터

기존 Express 서버에는 없던 기능. 프론트엔드 검색 UI와 연동.

**이미 4번 Post API에 통합 설계됨.**

`GET /api/posts?status=RECRUITING&role=DEVELOPER&keyword=앱&page=0&size=10`

**구현 범위:**
- `PostRepository`에 동적 쿼리 추가
- `Specification` 또는 `@Query`로 keyword, status, role 복합 필터
- 검색 결과 없을 때 빈 페이지 반환 (예외 아님)

---

### 10. 예외 처리 & 공통 응답

**구현 범위:**
- `GlobalExceptionHandler` (`@RestControllerAdvice`)
- 공통 응답 포맷: `ApiResponse<T>` (`success`, `data`, `message`)
- 커스텀 예외: `CustomException` + `ErrorCode` Enum
- 주요 에러 코드:
  - `MEMBER_NOT_FOUND`, `POST_NOT_FOUND`, `UNAUTHORIZED`, `FORBIDDEN`
  - `DUPLICATE_APPLICATION`, `ALREADY_REVIEWED`
  - `INVALID_TOKEN`, `EXPIRED_TOKEN`

---

### 11. 보안 강화

**구현 범위:**
- CORS 설정 (허용 오리진 명시: `http://localhost:5173`)
- 입력값 검증: `@Valid` + Bean Validation (`@NotBlank`, `@Size`, `@Email` 등)
- SQL Injection 방어: JPA 파라미터 바인딩으로 자동 처리
- XSS 방어: 응답 헤더 설정

---

## 개발 우선순위

```
1단계 (필수 기반)
  ├── 1. 도메인 모델 & DB 설계
  ├── 2. 인증 (JWT)
  └── 10. 예외 처리 & 공통 응답

2단계 (핵심 기능)
  ├── 3. 회원
  ├── 4. 프로젝트 포스트 (+ 9. 검색/필터 포함)
  ├── 5. 지원
  └── 7. 리뷰

3단계 (협업 기능)
  ├── 6. 채팅 (WebSocket)
  └── 8. 알림

4단계 (보안)
  └── 11. 보안 강화
```

---

## 실행 방법

### Backend (devsign_server)
```bash
cd devsign_server/api

# 빌드
./gradlew build

# 실행 (기본 포트 8080)
./gradlew bootRun
```

### Frontend (devsign_client)
```bash
cd devsign_client
npm install
npm run dev   # http://localhost:5173
```

---

## 패키지 구조 (예정)

```
devsign_server.api/
├── domain/
│   ├── member/
│   │   ├── entity/Member.java
│   │   ├── repository/MemberRepository.java
│   │   ├── service/MemberService.java
│   │   └── controller/MemberController.java
│   ├── post/
│   ├── application/
│   ├── message/
│   ├── review/
│   └── notification/
├── global/
│   ├── auth/           # JWT, SecurityConfig
│   ├── common/         # ApiResponse, BaseEntity
│   ├── exception/      # GlobalExceptionHandler, ErrorCode
│   └── config/         # WebSocketConfig, CorsConfig
└── ApiApplication.java
```
