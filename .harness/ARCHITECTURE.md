# ARCHITECTURE — 기술 스택 및 구조 명세

> 새 세션/도구가 코드베이스를 처음부터 다시 탐색하지 않도록 구조·스키마·API 명세를 한 곳에 모은다.
> 구조가 바뀌면 이 문서를 갱신한다 (전수 재작성 대신 변경분만 반영).
> 과거 `/SPEC.md`, `/frontend/SPEC.md`, `/backend/SPEC.md` 세 문서를 통합한 문서다.

## 목차

1. [개요 및 모노레포 구조](#개요-및-모노레포-구조)
2. [Prerequisites](#prerequisites)
3. [배포 아키텍처](#배포-아키텍처)
4. [Frontend 명세](#frontend-명세)
5. [Backend 명세](#backend-명세)
6. [DB 스키마](#db-스키마)
7. [API 명세 (구현 예정)](#api-명세-구현-예정)
8. [Frontend ↔ Backend 연동 상태](#frontend--backend-연동-상태)

---

## 개요 및 모노레포 구조

AIverse는 AI 생성 콘텐츠(이미지/영상/음악) 등록·탐색·크레딧 결제·구매 마켓플레이스. 서비스 기획은 `/README.md` 참조.

```
aiverse/
├── README.md
├── .harness/            # 이 문서를 포함한 크로스 툴 연속성 산출물
├── frontend/            # React 18 + Vite + TailwindCSS
└── backend/             # Java 21 + Spring Boot + Gradle
```

## Prerequisites

| 영역 | 필요 도구 |
| --- | --- |
| Frontend | Node.js >= 20.19.0, Yarn |
| Backend | Java 21, Gradle (Wrapper 포함), MySQL |

## 배포 아키텍처

```
┌─────────────┐       REST API        ┌─────────────┐
│  Frontend   │  ──────────────────►  │   Backend   │
│  (Vercel)   │  ◄──────────────────  │  (Spring)   │
└─────────────┘       JSON            └──────┬──────┘
                                             │
                                             ▼
                                        ┌─────────┐
                                        │  MySQL  │
                                        └─────────┘
```

- **Frontend** — Vercel 배포, port 5173 (로컬)
- **Backend** — 별도 클라우드 배포, port 8080 (로컬)
- **Database** — MySQL

---

## Frontend 명세

### Tech Stack

| 항목 | 버전 / 도구 |
| --- | --- |
| Runtime | Node.js >= 20.19.0 |
| Package Manager | Yarn |
| Framework | React 18 |
| Build Tool | Vite 8 |
| Styling | TailwindCSS 3, shadcn/ui |
| Routing | React Router 7 |
| State | Zustand, TanStack Query |
| Form | React Hook Form, Zod |

### 폴더 구조

```
frontend/
├── public/                  # 정적 파일
├── src/
│   ├── api/                 # API 클라이언트 레이어
│   │   ├── vibexClient.js   # VibeX SDK 클라이언트 (전환 예정)
│   │   ├── entities.js      # Entity API 래퍼
│   │   └── integrations.js  # 통합 API (파일 업로드 등)
│   ├── components/          # 공통 UI 컴포넌트
│   │   └── ui/              # shadcn/ui 기반 컴포넌트
│   ├── hooks/                # 커스텀 훅
│   ├── lib/                 # 인증, 유틸, 프로바이더
│   ├── pages/                # 페이지 컴포넌트
│   ├── sdk/                  # VibeX SDK 소스 (전환 예정)
│   ├── stores/                # Zustand 스토어
│   ├── App.jsx               # 라우터 및 앱 진입점
│   ├── Layout.jsx             # 공통 레이아웃 (Navbar, Footer)
│   ├── main.jsx               # React DOM 마운트
│   └── pages.config.js        # 페이지·레이아웃 설정
├── vite-plugins/              # Vite 개발용 플러그인 (production 빌드 제외)
├── index.html
├── vite.config.js
├── tailwind.config.js
├── package.json
└── components.json            # shadcn/ui 설정
```

### 라우팅

`pages.config.js`에서 페이지를 등록하고, `App.jsx`에서 React Router로 매핑.

| 경로 | 컴포넌트 | 파일 | 설명 |
| --- | --- | --- | --- |
| `/` | Home | `pages/Home.jsx` | 서비스 소개, 인기 콘텐츠 |
| `/Explore` | Explore | `pages/Explore.jsx` | 콘텐츠 탐색 (유형·카테고리·정렬 필터) |
| `/content/:slug` | ContentDetail | `pages/ContentDetail.jsx` | 콘텐츠 상세 및 구매 |
| `/Credits` | Credits | `pages/Credits.jsx` | 크레딧 충전 |
| `/Upload` | Upload | `pages/Upload.jsx` | AI 창작물 등록 |
| `/Library` | Library | `pages/Library.jsx` | 구매 보관함 |
| `/Dashboard` | Dashboard | `pages/Dashboard.jsx` | 창작자 판매 대시보드 |
| `/Profile` | Profile | `pages/Profile.jsx` | 프로필 수정 |
| `/Login`, `/Register` | Login, Register | `pages/Login.jsx`, `pages/Register.jsx` | 인증 |

### API 연동 — 현재 (VibeX SDK)

| 모듈 | 용도 |
| --- | --- |
| `entities.js` | Content, Category, User, Purchase, CreditTransaction CRUD |
| `vibexClient.js` | VibeX API 클라이언트 (`app.vibe-x.app`) |
| `integrations.js` | 파일 업로드, 프로젝트 정보 조회 |
| `sdk/index.js` | VibeX SDK 구현체 |

인증 토큰은 `localStorage.access_token`에 저장.

### API 연동 — 전환 예정 (Spring Boot REST API)

| VibeX SDK | Spring API (예정) |
| --- | --- |
| `Auth.login` | `POST /api/auth/login` |
| `Auth.register` | `POST /api/auth/register` |
| `vibex.auth.me()` | `GET /api/auth/me` |
| `Content.paging` | `GET /api/contents` |
| `Category.paging` | `GET /api/categories` |
| `UploadFile` | `POST /api/files/upload` |

### Frontend 환경 변수

| 변수 | 설명 | 예시 |
| --- | --- | --- |
| `VITE_API_URL` | Spring API Base URL (전환 후) | `http://localhost:8080/api` |
| `VITE_VIBEX_APP_ID` | VibeX App ID (현재) | — |
| `VITE_VIBEX_BACKEND_URL` | VibeX API URL (현재) | — |
| `VITE_APP_ENV` | `production` 시 VibeX dev 플러그인 비활성화 | `production` |

### Frontend Scripts

| Command | Description |
| --- | --- |
| `yarn dev` | 개발 서버 (port 5173) |
| `yarn dev:low-mem` | 저메모리 모드 개발 서버 (256MB) |
| `yarn build` | 프로덕션 빌드 → `dist/` |
| `yarn preview` | 프로덕션 빌드 미리보기 |

### Frontend 주요 의존성

| 패키지 | 용도 |
| --- | --- |
| `@tanstack/react-query` | 서버 상태 캐싱 |
| `zustand` | 클라이언트 전역 상태 (`useAppStore`) |
| `react-router-dom` | SPA 라우팅 |
| `framer-motion` | 애니메이션 |
| `lucide-react` | 아이콘 |
| `recharts` | 차트 (대시보드) |

---

## Backend 명세

### Tech Stack

| 항목 | 버전 / 도구 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Web | Spring Web MVC |
| ORM | Spring Data JPA |
| Database | MySQL |
| Build | Gradle 9.5.1 (Wrapper 포함) |
| Utility | Lombok |

### 폴더 구조 (현재)

```
backend/
├── build.gradle             # 의존성 및 빌드 설정
├── settings.gradle          # 프로젝트명 (aiverse)
├── gradlew / gradlew.bat    # Gradle Wrapper
├── gradle/wrapper/
└── src/
    ├── main/
    │   ├── java/com/example/aiverse/
    │   │   └── AiverseApplication.java    # 진입점
    │   └── resources/
    │       └── application.yaml           # 앱 설정
    └── test/
        └── java/com/example/aiverse/
            └── AiverseApplicationTests.java
```

### 구현 예정 패키지 구조

```
com.example.aiverse/
├── config/          # CORS, Security, JPA 설정
├── controller/      # REST API 엔드포인트
├── service/         # 비즈니스 로직
├── repository/      # JPA Repository
├── entity/          # JPA Entity
├── dto/             # Request / Response DTO
└── exception/       # 예외 처리
```

### Gradle Commands

| Command | Description |
| --- | --- |
| `./gradlew bootRun` | 개발 서버 실행 (port 8080) |
| `./gradlew build` | 컴파일 및 테스트 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew bootJar` | 실행 가능 JAR 생성 |

### 설정 (`application.yaml`)

현재:
```yaml
spring:
  application:
    name: aiverse
```

MySQL 연동 시 추가 예정:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiverse
    username: ...
    password: ...
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
```

### Backend 환경 변수

| 변수 | 설명 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |

### CORS

프론트엔드 도메인에서 API 호출을 허용해야 한다.
```
http://localhost:5173          # 로컬 개발
https://*.vercel.app            # Vercel 배포
```

### 도메인 개요

| 엔티티 | 설명 |
| --- | --- |
| User | 사용자 (창작자 / 구매자) |
| Asset | AI 창작물 (이미지, 영상, 음악) — README/SPEC에서는 "Content"로도 지칭 |
| Tag / AssetTag | 태그 및 콘텐츠-태그 연결 |
| CreditProduct | 크레딧 충전 상품 |
| Payment | 결제 이력 (목업) |
| CreditTransaction | 크레딧 증감 이력 |
| Purchase | 콘텐츠 구매 이력 |
| Download | 다운로드 이력 |
| CreatorSettlement | 창작자 정산 |

---

## DB 스키마

### user

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| email | varchar | unique |
| password | varchar | |
| nickname | varchar | |
| role | varchar | |
| profile_url | varchar | nullable |
| introduction | text | nullable |
| credit_balance | int | |
| created_at | datetime | |
| updated_at | datetime | nullable |

### asset

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| creator_id | bigint | FK → user |
| title | varchar | |
| description | varchar | nullable |
| asset_type | varchar | image / video / music |
| category | varchar | |
| preview_url | varchar | nullable |
| file_url | varchar | |
| price_credit | int | |
| ai_tool | varchar | nullable |
| license_type | varchar | |
| created_at | datetime | |
| updated_at | datetime | nullable |

### tag

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| name | varchar | unique |
| created_at | datetime | |

### asset_tag

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| asset_id | bigint | FK → asset |
| tag_id | bigint | FK → tag |

### credit_product

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| name | varchar | |
| credit_amount | int | |
| bonus_credit | int | |
| price | int | |
| status | varchar | |
| created_at | datetime | |

### payment

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | FK → user |
| credit_product_id | bigint | FK → credit_product |
| amount | int | |
| method | varchar | |
| status | varchar | |
| transaction_key | varchar | nullable |
| paid_at | varchar | nullable |
| failed_reason | varchar | nullable |
| created_at | datetime | |

### credit_transaction

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | FK → user |
| payment_id | varchar | nullable |
| type | varchar | |
| amount | int | |
| balance_after | int | |
| reason | varchar | |
| created_at | datetime | |

### purchase

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | FK → user |
| asset_id | bigint | FK → asset |
| credit_transaction_id | bigint | FK → credit_transaction |
| purchase_price_credit | int | |
| license_type | varchar | |
| purchased_at | datetime | |

### download

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| purchase_id | bigint | FK → purchase |
| user_id | bigint | FK → user |
| asset_id | bigint | FK → asset |
| downloaded_at | datetime | |

### creator_settlement

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| creator_id | bigint | FK → user |
| purchase_id | bigint | FK → purchase |
| asset_id | bigint | FK → asset |
| gross_credit | int | |
| platform_fee_credit | int | |
| settlement_credit | int | |
| status | varchar | |
| created_at | datetime | |
| settled_at | datetime | nullable |

<details>
<summary>전체 CREATE TABLE 문 (DDL 참고)</summary>

```sql
CREATE TABLE `asset` (
	`id` bigint NOT NULL,
	`creator_id` bigint NOT NULL,
	`title` varchar NOT NULL,
	`description` varchar NULL,
	`asset_type` varchar NOT NULL,
	`category` varchar NOT NULL,
	`preview_url` varchar NULL,
	`file_url` varchar NOT NULL,
	`price_credit` int NOT NULL,
	`ai_tool` varchar NULL,
	`license_type` varchar NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NULL
);

CREATE TABLE `credit_product` (
	`id` bigint NOT NULL,
	`name` varchar NULL,
	`credit_amount` int NULL,
	`bonus_credit` int NULL,
	`price` int NULL,
	`status` varchar NULL,
	`created_at` datetime NULL
);

CREATE TABLE `user` (
	`id` bigint NOT NULL,
	`email` varchar NOT NULL COMMENT 'unique',
	`password` varchar NOT NULL,
	`nickname` varchar NOT NULL,
	`role` varchar NOT NULL,
	`profile_url` varchar NULL,
	`introduction` text NULL,
	`credit_balance` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NULL
);

CREATE TABLE `download` (
	`id` bigint NOT NULL,
	`purchase_id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`downloaded_at` datetime NOT NULL
);

CREATE TABLE `asset_tag` (
	`id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`tag_id` bigint NOT NULL
);

CREATE TABLE `credit_transaction` (
	`id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`payment_id` varchar NULL,
	`type` varchar NOT NULL,
	`amount` int NOT NULL,
	`balance_after` int NOT NULL,
	`reason` varchar NOT NULL,
	`created_at` datetime NOT NULL
);

CREATE TABLE `purchase` (
	`id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`credit_transaction_id` bigint NOT NULL,
	`purchase_price_credit` int NOT NULL,
	`license_type` varchar NOT NULL,
	`purchased_at` datetime NOT NULL
);

CREATE TABLE `creator_settlement` (
	`id` bigint NOT NULL,
	`creator_id` bigint NOT NULL,
	`purchase_id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`gross_credit` int NOT NULL,
	`platform_fee_credit` int NOT NULL,
	`settlement_credit` int NOT NULL,
	`status` varchar NOT NULL,
	`created_at` datetime NOT NULL,
	`settled_at` datetime NULL
);

CREATE TABLE `payment` (
	`id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`credit_product_id` bigint NOT NULL,
	`amount` int NOT NULL,
	`method` varchar NOT NULL,
	`status` varchar NOT NULL,
	`transaction_key` varchar NULL,
	`paid_at` varchar NULL,
	`failed_reason` varchar NULL,
	`created_at` datetime NOT NULL
);

CREATE TABLE `tag` (
	`id` bigint NOT NULL,
	`name` varchar NOT NULL COMMENT 'unique',
	`created_at` datetime NOT NULL
);
```

</details>

---

## API 명세 (구현 예정)

### Auth

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → JWT 반환 |
| GET | `/api/auth/me` | 현재 사용자 정보 |

### Asset (Content)

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/contents` | 목록 (페이지·필터·정렬) |
| GET | `/api/contents/{id}` | 상세 |
| POST | `/api/contents` | 등록 (창작자) |
| PUT | `/api/contents/{id}` | 수정 |
| DELETE | `/api/contents/{id}` | 삭제 |

### Category / Tag

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/categories` | 카테고리 목록 |
| GET | `/api/tags` | 태그 목록 |

### Credit / Payment

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/credit-products` | 충전 상품 목록 |
| POST | `/api/payments` | 결제 요청 (목업) |
| GET | `/api/credit-transactions` | 크레딧 거래 이력 |

### Purchase / Library

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/purchases` | 콘텐츠 구매 |
| GET | `/api/library` | 구매 보관함 |
| POST | `/api/downloads` | 다운로드 기록 |

### Dashboard

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/dashboard/sales` | 창작자 판매 통계 |

### File

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/files/upload` | 파일 업로드 |

---

## Frontend ↔ Backend 연동 상태

| 영역 | 현재 | 목표 |
| --- | --- | --- |
| Frontend API | VibeX SDK | Spring Boot REST API |
| Frontend 배포 | VibeX 도메인 | Vercel |
| Backend | Spring Boot 초기 프로젝트 (Entity·API 미구현) | Entity·API 구현 |
