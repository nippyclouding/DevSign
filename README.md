# DevSign Connect

> 개발자와 디자이너가 만나 포트폴리오 프로젝트·공모전을 함께 완성하는 협업 커뮤니티

---

## 목차

1. [프로젝트 소개](#프로젝트-소개)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [실행 방법](#실행-방법)
5. [주요 기능](#주요-기능)
6. [API 명세](#api-명세)
7. [공통 응답 형식](#공통-응답-형식)

---

## 프로젝트 소개

DevSign Connect는 **개발자 ↔ 디자이너** 협업 매칭 플랫폼입니다.
프로젝트 모집부터 팀 구성, 실시간 소통, 완료 후 상호 리뷰까지 하나의 서비스에서 이루어집니다.

핵심 기능, 설명 
프로젝트 모집 - 팀원이 필요한 사람이 모집글을 올리고 개발자·디자이너를 모집 
지원 & 승인 - 관심 프로젝트에 지원하고, 팀장이 승인하면 팀원 확정 
실시간 채팅 - 승인된 팀원끼리 그룹 채팅으로 실시간 소통 
리뷰 & 평판 - 프로젝트 완료 후 팀원을 리뷰하고 평판 점수 누적 
알림 - 지원·승인·거절·완료 이벤트에 대한 자동 알림 

<img width="1275" height="988" alt="sign1" src="https://github.com/user-attachments/assets/7a47c3f6-7e9c-4afa-a1b4-3084d04eddb3" />

---
## 기술 스택

### Frontend

| 분류 | 기술 |
|------|------|
| Framework | React 19, TypeScript |
| Build | Vite 6 |
| Styling | Tailwind CSS v4 |
| Animation | Motion (Framer Motion) |
| 실시간 | SockJS + STOMP (`@stomp/stompjs`) |
| 날짜 처리 | date-fns |
| 아이콘 | Lucide React |
| 상태 관리 | React Context API |

### Backend

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 4.0.3 |
| Language | Java 17 |
| Build | Gradle |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security |
| 인증 | JWT (JJWT 0.12, Stateless) |
| 실시간 | Spring WebSocket + STOMP |
| Database | PostgreSQL |
| 유틸 | Lombok, Spring Validation |

---

## ERD
<img width="1366" height="900" alt="erd" src="https://github.com/user-attachments/assets/46343224-813f-4ab3-b810-7cdcd8cc58f1" />


---

## 주요 기능

### 회원가입 / 로그인

- 이메일 기반 가입, 역할(`developer` / `designer`) 선택
- 비밀번호 BCrypt 해싱 저장
- 로그인 성공 시 JWT 발급, 이후 `Authorization: Bearer <token>` 헤더로 인증

### 프로젝트 모집

- 모집글 생성 시 **그룹 채팅방 자동 생성**
- 상태 흐름: `RECRUITING(모집중)` → `PROGRESS(진행중)` → `COMPLETED(완료)`
- 상태 변경은 **작성자만** 가능
- 필요 인원(개발자 수 / 디자이너 수) 별도 지정

### 지원 시스템

- 본인 프로젝트 지원 불가, 중복 지원 불가
- 지원 상태: `PENDING` → `APPROVED` / `REJECTED`
- 승인된 지원자 = 프로젝트 참여자 (채팅방 입장 가능)

### 실시간 채팅 (STOMP/WebSocket)

- 작성자 + 승인된 팀원만 채팅방 접근 가능
- STOMP CONNECT 헤더로 JWT 인증
- 채팅 메시지 DB 영구 저장
- 입장 시 기존 채팅 히스토리 자동 로드

```
WebSocket 연결:  /ws  (SockJS)
구독 토픽:       /sub/chat/{groupChatId}
메시지 발행:     /pub/chat/{groupChatId}
```

### 리뷰 & 평판

- 프로젝트가 `COMPLETED` 상태일 때만 리뷰 작성 가능
- 동일 프로젝트 내 reviewer → reviewee 방향으로 1회만 작성
- 리뷰 별점(1~5)이 대상자의 `reputation` 점수에 누적

### 알림

| 이벤트 | 수신자 |
|--------|--------|
| 내 프로젝트에 새 지원자 발생 | 프로젝트 작성자 |
| 내 지원이 승인됨 | 지원자 |
| 내 지원이 거절됨 | 지원자 |
| 프로젝트 완료 (리뷰 요청) | 전체 참여자 |

---

## API 명세

### 인증 `/api/auth`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/auth/signup` | 회원가입 | |
| `POST` | `/api/auth/login` | 로그인 → JWT 발급 | |
| `GET` | `/api/auth/me` | 내 정보 조회 | ✓ |

---

### 회원 `/api/members`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/members/{id}` | 회원 프로필 조회 | |
| `GET` | `/api/members/{id}/reviews` | 회원이 받은 리뷰 목록 | |
| `PUT` | `/api/members/me` | 내 프로필 수정 (이름, 소개) | ✓ |
| `DELETE` | `/api/members/me` | 계정 삭제 | ✓ |
| `GET` | `/api/members/me/projects` | 내가 만들거나 참여한 프로젝트 | ✓ |

---

### 프로젝트 `/api/projects`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/projects` | 목록 조회 (필터·검색·페이징) | |
| `GET` | `/api/projects/{id}` | 상세 조회 | |
| `POST` | `/api/projects` | 모집글 생성 | ✓ |
| `PATCH` | `/api/projects/{id}/status` | 상태 변경 (작성자만) | ✓ |

**쿼리 파라미터**

```
GET /api/projects?status=RECRUITING&section=DEVELOPER&keyword=앱&page=0&size=10
```

| 파라미터 | 값 | 기본값 |
|----------|----|--------|
| `status` | `RECRUITING` / `PROGRESS` / `COMPLETED` | 전체 |
| `section` | `DEVELOPER` / `DESIGNER` | 전체 |
| `keyword` | 검색어 (제목·내용) | 없음 |
| `page` | 페이지 번호 | `0` |
| `size` | 페이지 크기 | `10` |

---

### 지원 `/api/applicants`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/projects/{id}/apply` | 프로젝트 지원 | ✓ |
| `GET` | `/api/projects/{id}/applicants` | 지원자 목록 (작성자·팀원만) | ✓ |
| `PATCH` | `/api/applicants/{id}/status` | 승인 / 거절 (작성자만) | ✓ |

---

### 채팅 `/api/projects/{id}/messages`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/projects/{id}/messages` | 채팅 히스토리 (팀원만) | ✓ |

**WebSocket (STOMP)**

| 항목 | 값 |
|------|----|
| 엔드포인트 | `/ws` (SockJS) |
| 인증 방식 | STOMP CONNECT 헤더: `Authorization: Bearer <token>` |
| 구독 | `/sub/chat/{groupChatId}` |
| 발행 | `/pub/chat/{groupChatId}` |
| 메시지 Body | `{ "content": "메시지 내용" }` |

---

### 리뷰 `/api/reviews`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/reviews` | 리뷰 작성 (완료 프로젝트만) | ✓ |

---

### 알림 `/api/notifications`

| Method | URL | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/notifications` | 알림 목록 조회 | ✓ |
| `PATCH` | `/api/notifications/{id}/read` | 단건 읽음 처리 | ✓ |
| `PATCH` | `/api/notifications/read-all` | 전체 읽음 처리 | ✓ |

---

## 공통 응답 형식

모든 API 응답은 아래 형식으로 감싸져 반환됩니다.

**성공**
```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

**실패**
```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지"
}
```

목록 조회 시 `data`는 Spring Page 객체:
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "total_pages": 5,
    "total_elements": 47,
    "number": 0,
    "size": 10
  }
}
```
