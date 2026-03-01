# DevSign Connect

개발자와 디자이너가 만나 포트폴리오 프로젝트와 공모전을 함께 진행하는 커뮤니티 플랫폼.

---

## 프로젝트 구조

```
Devsign/
└── devsign_client/          # 풀스택 애플리케이션 (React + Express + SQLite)
    ├── src/
    │   ├── pages/           # 페이지 컴포넌트
    │   ├── components/      # 공통 컴포넌트
    │   ├── lib/             # 유틸리티
    │   ├── types.ts         # TypeScript 타입 정의
    │   └── App.tsx          # 라우터
    ├── server.ts            # Express + WebSocket 백엔드
    ├── package.json
    ├── vite.config.ts
    └── tsconfig.json
```

## 기술 스택

| 영역 | 기술 |
|------|------|
| Frontend | React 19, TypeScript, Vite, Tailwind CSS v4 |
| Animation | Motion (Framer Motion) |
| Icons | lucide-react |
| Backend | Express, better-sqlite3 |
| Auth | JWT (jsonwebtoken), bcryptjs |
| Realtime | WebSocket (ws) |
| AI | @google/genai (Gemini) |

---

## 실행 방법

```bash
cd devsign_client

# 패키지 설치
npm install

# 환경변수 설정 (.env.local 생성)
GEMINI_API_KEY=your_key_here
APP_URL=http://localhost:3000

# 개발 서버 실행 (포트 3000)
npm run dev
```

---

## 현재 구현된 기능

### 인증
- 회원가입 (이름, 역할 선택, 이메일, 비밀번호)
- 로그인 / 로그아웃
- JWT 기반 인증 상태 유지

### 프로젝트 관리
- 프로젝트 포스트 생성 (모집 인원, 기간, 내용)
- 프로젝트 목록 조회 (페이지네이션, 10개씩)
- 상태 필터 (모집중 / 진행중 / 완료)
- 프로젝트 상세 모달
- 프로젝트 상태 변경 (작성자만)

### 지원 & 협업
- 프로젝트 지원 신청
- 지원자 목록 조회 (작성자 / 승인된 멤버)
- 지원 승인 / 거절 (작성자만)
- 실시간 그룹 채팅 (WebSocket, 승인된 멤버만)
- 채팅 기록 영구 저장

### 리뷰 & 평판
- 완료된 프로젝트 리뷰 작성
- 평판 점수 시스템 (리뷰 평점 반영)
- 마이페이지에서 받은 리뷰 확인

### 사용자 프로필
- 프로필 수정 (이름, 포트폴리오/소개)
- 내가 만든 프로젝트 / 참여한 프로젝트 목록
- 계정 삭제

---

## 개발해야 할 기능 명세

---

### 1. 검색 기능
**우선순위:** 높음

MainPage에 검색 인풋 UI는 존재하지만, 백엔드 검색 로직이 전혀 구현되어 있지 않다.

**구현 범위:**
- `GET /api/posts?search=keyword` 쿼리 파라미터 처리
- SQLite `LIKE` 쿼리로 `main_title`, `subtitle`, `content` 필드 검색
- 프론트엔드 검색 입력 → API 호출 연동 (디바운싱 적용)
- 검색 결과 없을 때 빈 상태 UI 표시

---

### 2. 역할별 모집 필터링
**우선순위:** 높음

현재 상태(모집중/진행중/완료)만 필터링 가능하며, "개발자 필요" / "디자이너 필요" 여부로 필터링하는 기능이 없다.

**구현 범위:**
- `GET /api/posts?role=developer|designer` 필터 파라미터 추가
- `needed_developers > 0` 또는 `needed_designers > 0` 조건 쿼리
- MainPage 필터 UI에 역할 필터 버튼 추가
- 상태 필터와 역할 필터 복합 적용

---

### 3. Gemini AI 연동
**우선순위:** 중간

`@google/genai` 패키지가 설치되어 있고 `GEMINI_API_KEY`도 환경변수로 설정되어 있으나, 실제 코드에서 전혀 사용되지 않는다.

**구현 후보 기능 (택일 또는 복합):**
- **프로젝트 설명 자동 생성:** CreatePostPage에서 제목/기간 입력 시 AI가 content 초안 작성
- **지원 메시지 도우미:** 지원 신청 시 AI가 자기소개 문구 제안
- **프로젝트 추천:** 사용자 역할과 과거 활동 기반 프로젝트 추천
- **리뷰 요약:** 다수의 리뷰를 AI로 요약해 프로필에 표시

**공통 구현 사항:**
- `server.ts`에 Gemini API 호출 엔드포인트 추가
- 스트리밍 응답 지원 (UX 향상)
- API 키 미설정 시 fallback 처리

---

### 4. 알림 시스템
**우선순위:** 중간

지원 승인/거절, 새 지원자, 리뷰 작성 등 중요 이벤트에 대한 알림이 전혀 없다.

**구현 범위:**
- DB에 `notifications` 테이블 추가
  ```sql
  notifications (id, user_id, type, message, related_id, is_read, created_at)
  ```
- 알림 생성 트리거:
  - 내 프로젝트에 새 지원자 → 작성자에게 알림
  - 지원 승인/거절 → 지원자에게 알림
  - 프로젝트 완료 후 리뷰 요청 → 참여자에게 알림
- `GET /api/notifications` — 내 알림 목록
- `POST /api/notifications/:id/read` — 읽음 처리
- Layout 네비게이션에 알림 벨 아이콘 + 미읽음 뱃지 표시
- WebSocket을 활용한 실시간 알림 푸시

---

### 5. 채팅 참가자 목록 실시간 연동
**우선순위:** 낮음

ChatPage의 참가자 목록이 실제 승인된 멤버와 연동되지 않고 하드코딩된 아바타를 사용한다.

**구현 범위:**
- ChatPage 마운트 시 `GET /api/posts/:id/applications` 호출해 승인된 참가자 목록 조회
- 실제 사용자 이름, 역할을 참가자 패널에 렌더링
- 현재 채팅방에 접속 중인 유저 실시간 표시 (WebSocket presence)
- 하드코딩 아바타 제거

---

### 6. 개인 다이렉트 메시지 (DM)
**우선순위:** 낮음

현재는 프로젝트 단위 그룹 채팅만 존재하며 1:1 메시지 기능이 없다.

**구현 범위:**
- `direct_messages` 테이블 추가 (sender_id, receiver_id, content, created_at)
- `GET /api/dm/:userId` — 특정 유저와의 DM 내역
- `POST /api/dm/:userId` — DM 전송
- WebSocket 채널 분리 (그룹 채팅 vs DM)
- UserProfileModal에 "DM 보내기" 버튼 추가
- DM 전용 페이지 또는 슬라이드 패널 UI

---

### 7. 보안 강화
**우선순위:** 높음 (배포 전 필수)

현재 코드에 여러 보안 취약점이 존재한다.

**구현 범위:**
- **XSS 방어:** 채팅 메시지, 리뷰 내용, 프로필 데이터 출력 시 이스케이프 처리
- **CORS 설정:** `express` CORS 미들웨어로 허용 오리진 명시적 제한
- **Rate Limiting:** `express-rate-limit`으로 `/api/auth/*` 엔드포인트 요청 횟수 제한
- **입력 유효성 검사:** 모든 API 엔드포인트에서 서버 사이드 유효성 검사 추가
- **JWT 갱신 토큰:** Access Token 단기 만료 + Refresh Token 구조 도입
- **HTTPS 강제:** 프로덕션 환경에서 HTTP → HTTPS 리다이렉트

---

### 8. 성능 최적화
**우선순위:** 낮음

**구현 범위:**
- **DB 인덱스 추가:**
  ```sql
  CREATE INDEX idx_posts_status ON posts(status);
  CREATE INDEX idx_posts_author ON posts(author_id);
  CREATE INDEX idx_applications_post ON applications(post_id);
  CREATE INDEX idx_messages_post ON messages(post_id);
  ```
- **코드 스플리팅:** React.lazy + Suspense로 페이지별 번들 분리
- **가상 스크롤:** 채팅 메시지가 많아질 경우 가상 리스트 적용
- **이미지 최적화:** 외부 이미지(Unsplash) WebP 변환 및 lazy loading
- **API 응답 캐싱:** 자주 조회되는 데이터에 캐시 헤더 적용

---

## 데이터베이스 스키마 (현재)

```sql
users         (id, email, password, name, role, reputation, profile_data)
posts         (id, author_id, main_title, subtitle, start_date, end_date, content, status, needed_developers, needed_designers, created_at)
applications  (id, post_id, applicant_id, status, created_at)
messages      (id, post_id, user_id, content, created_at)
reviews       (id, post_id, reviewer_id, reviewee_id, content, rating, created_at)
```

---

## API 엔드포인트 목록 (현재)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | 회원가입 | - |
| POST | `/api/auth/login` | 로그인 | - |
| GET | `/api/auth/me` | 내 정보 조회 | O |
| PUT | `/api/auth/profile` | 프로필 수정 | O |
| DELETE | `/api/auth/account` | 계정 삭제 | O |
| GET | `/api/posts` | 프로젝트 목록 | - |
| POST | `/api/posts` | 프로젝트 생성 | O |
| GET | `/api/posts/:id` | 프로젝트 상세 | - |
| POST | `/api/posts/:id/status` | 상태 변경 | O (작성자) |
| POST | `/api/posts/:id/apply` | 지원 신청 | O |
| GET | `/api/posts/:id/applications` | 지원자 목록 | O |
| POST | `/api/applications/:id/status` | 승인/거절 | O (작성자) |
| GET | `/api/posts/:id/messages` | 채팅 기록 | O (멤버) |
| GET | `/api/users/:id` | 유저 프로필 | - |
| GET | `/api/users/:id/reviews` | 유저 리뷰 목록 | - |
| POST | `/api/reviews` | 리뷰 작성 | O |
| GET | `/api/users/projects` | 내 프로젝트 목록 | O |
