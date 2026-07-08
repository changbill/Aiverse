# ARCHITECTURE — 기술 스택 및 구조 명세

> 새 세션/도구가 코드베이스를 처음부터 다시 탐색하지 않도록 구조·스키마·API 명세를 한 곳에 모은다.
> 구조가 바뀌면 이 문서를 갱신한다 (전수 재작성 대신 변경분만 반영).
> 과거 `/SPEC.md`, `/frontend/SPEC.md`, `/backend/SPEC.md` 세 문서를 통합한 문서다.
> **이 문서에 담지 않는 것:** 왜 이렇게 결정했는지(`DECISIONS.md` 몫), 지금 뭐가 끝났는지(`STATE.md` 몫).

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

| 영역     | 필요 도구                             |
| -------- | ------------------------------------- |
| Frontend | Node.js >= 20.19.0, Yarn              |
| Backend  | Java 21, Gradle (Wrapper 포함), MySQL |

## 배포 아키텍처

```
┌─────────────┐       REST API        ┌─────────────┐
│  Frontend   │  ──────────────────►  │   Backend   │
│  (Vercel)   │  ◄──────────────────  │  (Spring)   │
└─────────────┘       JSON            └──────┬──────┘
                                      ┌──────┴──────┐
                                      ▼             ▼
                                 ┌─────────┐   ┌──────────┐
                                 │  MySQL  │   │ S3/MinIO │
                                 └─────────┘   └──────────┘
```

- **Frontend** — Vercel 배포, port 5173 (로컬)
- **Backend** — 별도 클라우드 배포, port 8080 (로컬)
- **Database** — MySQL
- **Object Storage** — 운영 AWS S3, 로컬 개발 MinIO

---

## Frontend 명세

### Tech Stack

| 항목            | 버전 / 도구              |
| --------------- | ------------------------ |
| Runtime         | Node.js >= 20.19.0       |
| Package Manager | Yarn                     |
| Framework       | React 18                 |
| Build Tool      | Vite 8                   |
| Styling         | TailwindCSS 3, shadcn/ui |
| Routing         | React Router 7           |
| State           | Zustand, TanStack Query  |
| Form            | React Hook Form, Zod     |

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

| 경로                  | 컴포넌트        | 파일                                    | 설명                                  |
| --------------------- | --------------- | --------------------------------------- | ------------------------------------- |
| `/`                   | Home            | `pages/Home.jsx`                        | 서비스 소개, 인기 콘텐츠              |
| `/Explore`            | Explore         | `pages/Explore.jsx`                     | 콘텐츠 탐색 (유형·카테고리·정렬 필터) |
| `/content/:slug`      | ContentDetail   | `pages/ContentDetail.jsx`               | 콘텐츠 상세 및 구매                   |
| `/Credits`            | Credits         | `pages/Credits.jsx`                     | 크레딧 충전                           |
| `/Upload`             | Upload          | `pages/Upload.jsx`                      | AI 창작물 등록                        |
| `/Library`            | Library         | `pages/Library.jsx`                     | 구매 보관함                           |
| `/Dashboard`          | Dashboard       | `pages/Dashboard.jsx`                   | 창작자 판매 대시보드                  |
| `/Profile`            | Profile         | `pages/Profile.jsx`                     | 프로필 수정                           |
| `/Login`, `/Register` | Login, Register | `pages/Login.jsx`, `pages/Register.jsx` | 인증                                  |

### API 연동 — 현재 (VibeX SDK)

| 모듈              | 용도                                                      |
| ----------------- | --------------------------------------------------------- |
| `entities.js`     | Content, Category, User, Purchase, CreditTransaction CRUD |
| `vibexClient.js`  | VibeX API 클라이언트 (`app.vibe-x.app`)                   |
| `integrations.js` | 파일 업로드, 프로젝트 정보 조회                           |
| `sdk/index.js`    | VibeX SDK 구현체                                          |

인증 토큰은 `localStorage.access_token`에 저장.

### API 연동 — 전환 예정 (Spring Boot REST API)

| VibeX SDK         | Spring API (예정)         |
| ----------------- | ------------------------- |
| `Auth.login`      | `POST /api/auth/login`    |
| `Auth.register`   | `POST /api/auth/register` |
| `vibex.auth.me()` | `GET /api/auth/me`        |
| `Content.paging`  | `GET /api/contents`       |
| `Category.paging` | `GET /api/categories`     |
| `UploadFile`      | `POST /api/files/upload`  |

### Frontend 환경 변수

| 변수                     | 설명                                        | 예시                        |
| ------------------------ | ------------------------------------------- | --------------------------- |
| `VITE_API_URL`           | Spring API Base URL (전환 후)               | `http://localhost:8080/api` |
| `VITE_VIBEX_APP_ID`      | VibeX App ID (현재)                         | —                           |
| `VITE_VIBEX_BACKEND_URL` | VibeX API URL (현재)                        | —                           |
| `VITE_APP_ENV`           | `production` 시 VibeX dev 플러그인 비활성화 | `production`                |

### Frontend Scripts

| Command            | Description                     |
| ------------------ | ------------------------------- |
| `yarn dev`         | 개발 서버 (port 5173)           |
| `yarn dev:low-mem` | 저메모리 모드 개발 서버 (256MB) |
| `yarn build`       | 프로덕션 빌드 → `dist/`         |
| `yarn preview`     | 프로덕션 빌드 미리보기          |

### Frontend 주요 의존성

| 패키지                  | 용도                                 |
| ----------------------- | ------------------------------------ |
| `@tanstack/react-query` | 서버 상태 캐싱                       |
| `zustand`               | 클라이언트 전역 상태 (`useAppStore`) |
| `react-router-dom`      | SPA 라우팅                           |
| `framer-motion`         | 애니메이션                           |
| `lucide-react`          | 아이콘                               |
| `recharts`              | 차트 (대시보드)                      |

---

## Backend 명세

### Tech Stack

| 항목      | 버전 / 도구                 |
| --------- | --------------------------- |
| Language  | Java 21                     |
| Framework | Spring Boot 4.1.0           |
| Web       | Spring Web MVC              |
| ORM       | Spring Data JPA             |
| Database  | MySQL                       |
| Migration | Flyway                      |
| Build     | Gradle 9.5.1 (Wrapper 포함) |
| Utility   | Lombok                      |
| Integration Test | Testcontainers MySQL   |
| API 문서  | springdoc-openapi (Swagger UI) |

### API 문서 (Swagger UI)

- `springdoc-openapi-starter-webmvc-ui`가 `/swagger-ui.html`(UI)과 `/v3/api-docs`(OpenAPI JSON)를 자동 제공한다.
- `config/OpenApiConfig`에서 API 제목과 JWT Bearer 보안 스키마(`bearer-jwt`)를 등록한다.
- Controller를 추가할 때마다 클래스에 `@Tag`, 각 엔드포인트 메서드에 `@Operation`을 붙인다. Access token이 필요한 엔드포인트는 `@SecurityRequirement(name = "bearer-jwt")`를 추가해 Swagger UI의 Authorize 버튼으로 바로 호출해볼 수 있게 한다.

### 폴더 구조 (현재)

```
backend/
├── build.gradle              # 의존성 및 빌드 설정
├── settings.gradle           # 프로젝트명 (aiverse)
├── docker-compose.yml        # 로컬 MySQL·MinIO
├── gradlew / gradlew.bat     # Gradle Wrapper
├── gradle/wrapper/
└── src/
    ├── main/
    │   ├── java/com/example/aiverse/
    │   │   ├── AiverseApplication.java    # 진입점
    │   │   ├── common/                    # 공통 응답·오류·요청 ID
    │   │   └── repository/                # Repository 계층 (아래 참조)
    │   │       ├── jpa/
    │   │       └── impl/
    │   └── resources/
    │       ├── application.yaml           # 공통 설정
    │       ├── application-{local,test,prod}.yaml
    │       └── db/migration/              # Flyway V1, V2 ...
    └── test/
        └── java/com/example/aiverse/
            ├── AiverseApplicationTests.java
            ├── common/
            └── support/                    # RepositoryIntegrationTestSupport, IntegrationTestSupport 등
```

### 구현 예정 패키지 구조

```
com.example.aiverse/
├── config/          # CORS, Security, JPA 설정
├── controller/      # REST API 엔드포인트
├── service/         # 비즈니스 로직
├── repository/      # Repository 계층 — 아래 "Repository 계층 구조" 참조
├── entity/          # JPA Entity
├── dto/             # Request / Response DTO
├── exception/       # 예외 처리
└── util/						 # 범용 유틸
```

### Repository 계층 구조

Repository는 도메인 인터페이스와 구현을 분리한 계층으로 구성한다. 서비스 계층은 항상 도메인 인터페이스에만 의존하고, Spring Data JPA·Querydsl 타입을 직접 참조하지 않는다.

```
repository/
├── {Entity}Repository.java             # 도메인 인터페이스 (서비스가 의존하는 포트)
├── jpa/
│   └── {Entity}JpaRepository.java      # Spring Data JpaRepository — 단순 CRUD/파생 쿼리 (Spring이 런타임에 생성)
├── querydsl/
│   └── {Entity}QuerydslRepository.java # 동적 쿼리·집계 전담 컴포넌트 (`@Repository`, `EntityManager` 주입, `JPAQueryFactory` 사용)
└── impl/
    └── {Entity}RepositoryImpl.java     # 도메인 인터페이스를 구현하는 어댑터 — {Entity}JpaRepository/{Entity}QuerydslRepository를 주입받아 각 메서드를 위임만 한다
```

- 서비스는 `{Entity}Repository`(인터페이스)만 주입받는다.
- `{Entity}RepositoryImpl`은 로직을 직접 갖지 않는다 — `{Entity}JpaRepository`(단순 CRUD)와 `{Entity}QuerydslRepository`(동적 쿼리·집계)를 생성자로 주입받아 메서드별로 위임만 한다.
- 동적 조건 조합·그룹핑 집계 등 Querydsl이 필요한 조회는 `{Entity}QuerydslRepository`에 구현한다. 이 클래스가 `EntityManager`를 주입받아 `JPAQueryFactory`를 만드는 유일한 지점이며, `{Entity}RepositoryImpl`은 `EntityManager`를 직접 다루지 않는다.
- `@DataJpaTest` 슬라이스는 `{Entity}RepositoryImpl`처럼 `{Entity}QuerydslRepository`도 컴포넌트 스캔하지 않으므로, `RepositoryIntegrationTestConfiguration`에 함께 등록해야 한다.
- 이 구조는 모든 모듈(Content/Credit/Purchase/Dashboard 등)의 Repository에 동일하게 적용한다.

### JPA 조회 최적화

- 연관관계 N+1은 명시적 JPQL/Querydsl fetch join으로 해결하며 `@EntityGraph`는 사용하지 않는다.
- 페이징 목록 쿼리는 `ManyToOne`·`OneToOne` 등 `XToOne` 연관관계만 fetch join한다. `XToMany` 컬렉션 fetch join과 페이징을 함께 사용하지 않는다.
- API 응답은 목록 DTO와 상세 DTO로 분리한다. 목록 DTO는 `XToOne` 연관관계만으로 구성하고, 컬렉션이 필요한 응답은 상세 DTO에서 제공한다.
- 페이징 목록에 컬렉션 정보가 필요하면 별도 일괄 조회, DTO projection, 집계 쿼리 등 DB 페이징을 유지하는 방식을 사용한다.

### 개발 방법론 — TDD

모든 백엔드 구현(Repository/Service/Controller)은 테스트 주도 개발(TDD)로 진행한다.

1. 요구사항을 검증하는 테스트를 먼저 작성하고 실패를 확인한다 (Repository는 `RepositoryIntegrationTestSupport` + Testcontainers MySQL, Service는 단위 테스트, Controller는 MockMvc 계약 테스트).
2. 테스트를 통과시키는 최소 구현을 작성한다.
3. 테스트가 계속 통과하는 상태를 유지하며 리팩터링한다.

구현 코드보다 테스트를 먼저 커밋하거나, 최소한 구현과 테스트를 같은 단위로 묶어 테스트 없는 구현이 남지 않도록 한다.

### 테스트 실행 정책

- 구현·수정 후 **기본 검증**은 단위 테스트만 실행한다 (`./gradlew test`).
- **TDD로 통합 테스트를 작성·수정하는 작업** 중에는 해당 통합 테스트를 반드시 실행한다 (해당 클래스만 `./gradlew integrationTest --tests ...`로 실행 가능).
- 통합 테스트 **전체 스위트**는 사용자가 명시적으로 요청한 경우에만 실행한다 (`./gradlew integrationTest`).
- 자세한 내용은 `CLAUDE.md`의 "하네스: 테스트 실행 정책" 참조.

### 통합 테스트 구조

| 베이스 | 어노테이션 | 용도 |
| --- | --- | --- |
| `RepositoryIntegrationTestSupport` | `@DataJpaTest` + Testcontainers | `*RepositoryTest` — JPA·Querydsl·Flyway 슬라이스 |
| `IntegrationTestSupport` | `@SpringBootTest` + Testcontainers | 앱 기동·Security 필터·MockMvc 전체 스택 |

- Testcontainers MySQL: Spring `@Bean` + `withReuse(true)`, `src/test/resources/testcontainers.properties`에서 재사용 활성화.
- 새 `*RepositoryImpl` 추가 시 `RepositoryIntegrationTestConfiguration`에 등록.
- 테스트별 `@Transactional` 롤백으로 데이터 격리.

### Gradle Commands

| Command             | Description                |
| ------------------- | -------------------------- |
| `./gradlew bootRun` | 개발 서버 실행 (port 8080) |
| `./gradlew build`   | 컴파일 및 단위 테스트      |
| `./gradlew test`    | 단위 테스트만 실행 (기본)  |
| `./gradlew integrationTest` | 통합 테스트만 실행 (TDD 작성 중 해당 클래스, 또는 사용자 요청 시 전체) |
| `./gradlew allTests` | 단위+통합 전체 테스트 실행 |
| `./gradlew bootJar` | 실행 가능 JAR 생성         |

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

### DB 마이그레이션과 환경 전략

- DB 스키마 생성·변경은 `src/main/resources/db/migration`의 Flyway SQL만 사용한다.
- Flyway 파일은 `V{번호}__{설명}.sql` 형식으로 작성하고 적용된 파일은 수정하지 않는다.
- 모든 프로필에서 JPA `ddl-auto=validate`를 사용해 Entity와 실제 스키마의 일치 여부만 검사한다.
- `local`, `test`, `prod` 프로필을 분리한다.
- 로컬 환경은 Docker Compose로 MySQL과 MinIO를 실행한다.
- Repository·트랜잭션 통합 테스트는 H2 대신 Testcontainers MySQL을 사용하고 Flyway 마이그레이션을 적용한다. Repository 테스트는 `@DataJpaTest` 슬라이스(`RepositoryIntegrationTestSupport`)로, 전체 스택 검증은 `@SpringBootTest`(`IntegrationTestSupport`)로 분리한다.
- DB 비밀번호와 Object Storage 접근 키는 설정 파일에 저장하지 않고 환경 변수로 주입한다.
- PK, FK, 유니크 제약과 목록·소유권·거래 이력 조회에 필요한 인덱스는 Flyway 마이그레이션에 명시한다.

### Backend 환경 변수

| 변수                         | 설명           |
| ---------------------------- | -------------- |
| `SPRING_DATASOURCE_URL`      | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자      |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호    |
| `STORAGE_ENDPOINT`           | S3 또는 MinIO API endpoint |
| `STORAGE_REGION`             | S3 region |
| `STORAGE_ACCESS_KEY`         | S3 또는 MinIO access key |
| `STORAGE_SECRET_KEY`         | S3 또는 MinIO secret key |
| `STORAGE_BUCKET`             | Object Storage bucket 이름 |
| `JWT_SECRET_KEY`             | Access token 서명 키 (HMAC-SHA384, 최소 48바이트) |
| `CORS_ALLOWED_ORIGINS`       | 허용할 프론트엔드 출처(쉼표 구분, `https://*.example.com` 같은 패턴 허용) |

로컬 개발에서는 `backend/.env`에 Docker Compose 변수와 Spring Boot 로컬 실행 변수를 함께 정리한다. `application-local.yaml`은 `${ENV:로컬기본값}` 형태를 유지해 `.env`를 로드하지 않아도 기본 Docker Compose 구성으로 바로 실행할 수 있게 한다. `application-test.yaml`은 외부 환경 변수에 의존하지 않는 테스트 전용 고정값을 사용하고, `application-prod.yaml`은 기본값 없이 환경 변수 주입을 필수로 한다.

**운영 배포(`docker compose up -d`, 예: 미니PC + Cloudflare Tunnel):** `backend/docker-compose.yml`의 `app` 서비스가 `Dockerfile`로 빌드된 백엔드 컨테이너를 `9999:8080`으로 노출한다. MinIO는 S3 API를 `9998:9000`(내부 `mysql`/`minio` 서비스명으로 앱 컨테이너와 통신)으로, 관리 콘솔은 `9001:9001`로 노출한다. `STORAGE_ENDPOINT`는 앱 컨테이너와 브라우저가 동일하게 사용하므로 내부 Docker 네트워크 주소가 아니라 MinIO를 외부에 노출한 공개 URL(Presigned URL이 이 주소로 발급됨)을 그대로 지정해야 한다. `.env`의 `SPRING_PROFILES_ACTIVE=prod`로 전환하고, `JWT_SECRET_KEY`·MinIO 자격증명(`MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD`, 앱의 `STORAGE_ACCESS_KEY`/`SECRET_KEY`로도 재사용)은 로컬 개발용과 분리된 값을 사용한다.

### CORS

프론트엔드 도메인에서 API 호출을 허용해야 한다.

```
http://localhost:5173          # 로컬 개발
https://*.vercel.app            # Vercel 배포
```

### 도메인 개요

| 엔티티            | 설명                                                                  |
| ----------------- | --------------------------------------------------------------------- |
| User              | 사용자 (창작자 / 구매자)                                              |
| RefreshToken      | 로그인 기기별 Refresh token 세션                                     |
| Asset             | AI 창작물 (이미지, 영상, 음악) — README/SPEC에서는 "Content"로도 지칭 |
| Category          | Asset 카테고리 및 노출 순서·활성 상태                                |
| Tag / AssetTag    | 태그 및 콘텐츠-태그 연결                                              |
| CreditProduct     | 크레딧 충전 상품                                                      |
| Payment           | 결제 이력 (목업)                                                      |
| CreditTransaction | 크레딧 증감 이력                                                      |
| Purchase          | 콘텐츠 구매 이력                                                      |
| Download          | 다운로드 이력                                                         |
| CreatorSettlement | 창작자 정산                                                           |

### 사용자 역할과 콘텐츠 권한

- 일반 사용자 `USER`는 콘텐츠 구매와 판매를 모두 할 수 있다.
- 창작자는 별도 역할이 아니라 콘텐츠를 등록한 사용자다.
- 콘텐츠 수정·삭제는 해당 콘텐츠의 `creator_id`와 인증 사용자 ID가 일치할 때만 허용한다.
- `ADMIN`은 향후 운영 기능을 위한 역할로 두며, 현재 API 범위에는 관리자 전용 기능을 포함하지 않는다.

### 인증 정책 (Spring Boot 전환 목표)

- Access token은 로그인·재발급 응답 본문으로 전달하고 프론트엔드 메모리에 저장한다.
- Access token 만료 시간은 15분이다.
- Refresh token은 `HttpOnly`, `Secure` 쿠키로 전달하며 만료 시간은 14일이다.
- Access token 만료 시 Refresh token 쿠키로 토큰을 재발급하고, 재발급할 때 Refresh token도 회전한다.
- 로그아웃 시 Refresh token을 폐기하고 쿠키를 제거한다.
- Access token 블랙리스트는 사용하지 않으며, 로그아웃 전에 발급된 Access token은 남은 만료 시간까지 유효하다.
- 프론트엔드 새로고침 시 재발급 API를 호출해 인증 상태를 복원한다.
- Refresh token은 로그인 기기별로 발급하며 MySQL `refresh_token` 테이블에 원문이 아닌 해시로 저장한다.
- 재발급 시 기존 Refresh token을 폐기하고 새 Refresh token으로 회전한다.
- 로그아웃은 요청에 포함된 Refresh token 세션만 폐기하며 다른 기기의 로그인은 유지한다.

### 회원가입과 계정 정책

- 회원가입 입력은 `email`, `password`, `nickname`이다.
- 이메일은 앞뒤 공백을 제거하고 소문자로 정규화한 뒤 유일성을 검사한다.
- 이메일 인증은 MVP 범위에서 제외한다.
- 비밀번호는 8자 이상 64자 이하로 제한하고 BCrypt 해시만 저장한다.
- 닉네임은 앞뒤 공백을 제거하고 2자 이상 20자 이하로 제한하며 중복을 허용하지 않는다.
- 회원가입 시 역할은 항상 `USER`, 초기 `credit_balance`는 0, 계정 상태는 `ACTIVE`다.
- 계정 상태는 `ACTIVE`, `DELETED`를 사용하되 회원 탈퇴 API는 MVP 범위에서 제외한다.
- 로그인 실패 시 이메일 존재 여부를 노출하지 않고 동일한 인증 실패 오류를 반환한다.
- 프론트엔드 회원가입의 `name` 필드는 `nickname`으로 변경한다.

### 파일 저장과 접근 정책

- 운영 환경은 AWS S3, 로컬 개발 환경은 S3 호환 MinIO를 사용한다.
- 원본 파일은 비공개 객체로 저장한다.
- 미리보기는 모든 콘텐츠 유형에서 별도 커버 이미지로 등록하고 인증 없이 조회할 수 있는 공개 객체로 저장한다.
- 백엔드는 업로드용 Presigned URL을 발급하고, 프론트엔드는 해당 URL로 Object Storage에 직접 업로드한다.
- 원본 다운로드 요청 시 백엔드가 구매 여부를 확인하고 다운로드 이력을 생성한 뒤, 짧은 유효기간의 Presigned URL을 반환한다.
- 썸네일 자동 생성, 영상 트랜스코딩, 음악 미리듣기 변환은 MVP 범위에서 제외한다.
- DB에는 만료되거나 환경에 종속되는 URL 대신 Object Storage 객체 키를 저장한다.
- API 응답 시 객체 키를 공개 미리보기 URL 또는 원본 Presigned URL로 변환한다.

업로드 허용 형식과 최대 크기:

| 용도 | Asset type | MIME / 확장자 | 최대 크기 |
| ---- | ---------- | ------------- | --------- |
| 미리보기 커버 | 전체 | JPEG, PNG, WebP | 10MB |
| 원본 | IMAGE | JPEG, PNG, WebP | 50MB |
| 원본 | VIDEO | MP4, WebM | 1GB |
| 원본 | MUSIC | MP3, WAV, FLAC | 200MB |

- Presigned URL 발급 전에 요청한 파일명 확장자, MIME type, 파일 크기를 검증한다.
- 업로드는 인증 사용자별 임시 객체 경로에만 허용한다.
- Asset 등록 시 S3/MinIO `HEAD` 요청으로 객체 존재 여부, 실제 크기, Content-Type과 요청 사용자의 임시 객체인지 다시 검증한다.
- 검증된 객체만 Asset에 연결하며 24시간 안에 Asset으로 등록되지 않은 임시 객체는 정리한다.
- 악성코드 검사와 미디어 내용 분석은 MVP 범위에서 제외한다.

### 콘텐츠 구매 트랜잭션 정책

- 본인이 등록한 콘텐츠는 구매할 수 없다.
- 동일 사용자는 같은 콘텐츠를 한 번만 구매할 수 있으며 `purchase(user_id, asset_id)` 유니크 제약으로 보장한다.
- 구매 API는 `Idempotency-Key` 요청 헤더를 필수로 받고 구매 이력에 저장한다.
- 동일 사용자가 같은 `Idempotency-Key`로 구매를 재요청하면 최초 처리 결과를 반환한다.
- 구매자와 창작자 행을 사용자 ID 오름차순으로 비관적 쓰기 잠금해 크레딧 변경을 직렬화하고 교착 상태를 방지한다.
- 콘텐츠 구매 가능 여부와 잔액 확인, 구매자 크레딧 차감, 창작자 크레딧 지급, 양쪽 `credit_transaction`, `purchase`, `creator_settlement` 생성을 하나의 DB 트랜잭션으로 처리한다.
- 처리 중 하나라도 실패하면 크레딧 차감을 포함한 모든 변경을 롤백한다.

### 크레딧 충전 목업 정책

- 결제 요청은 `credit_product_id`와 `Idempotency-Key` 요청 헤더를 받는다.
- 결제 금액, 기본 크레딧, 보너스 크레딧은 클라이언트 입력이 아니라 서버의 활성 `credit_product`를 기준으로 결정한다.
- 활성 상품에 대한 목업 결제는 즉시 성공하며 결제 수단은 `MOCK`, 상태는 `SUCCESS`로 기록한다.
- 사용자 행을 비관적 쓰기 잠금으로 조회하고 `payment` 생성, 크레딧 잔액 증가, `credit_transaction(CHARGE)` 생성을 하나의 DB 트랜잭션으로 처리한다.
- 동일 사용자가 같은 `Idempotency-Key`로 결제를 재요청하면 크레딧을 다시 지급하지 않고 최초 처리 결과를 반환한다.
- 인위적인 결제 실패 기능은 MVP에 포함하지 않으며, 비활성·존재하지 않는 상품 또는 시스템 오류만 실패 처리한다.
- 통화는 KRW를 사용한다.
- MVP에서는 활성 상품 조회 API만 제공하며 상품 관리 API는 제공하지 않는다.
- 초기 상품은 Flyway seed 데이터로 등록하고 운영에서 판매를 중단할 때 `INACTIVE`로 변경한다.

초기 크레딧 상품:

| code    | name  | 기본 크레딧 | 보너스 크레딧 | 가격(KRW) | display_order | status |
| ------- | ----- | ------------ | ------------- | --------- | ------------- | ------ |
| `BASIC` | Basic | 500          | 0             | 5,000     | 1             | ACTIVE |
| `PLUS`  | Plus  | 1,000        | 100           | 10,000    | 2             | ACTIVE |
| `PRO`   | Pro   | 3,000        | 500           | 30,000    | 3             | ACTIVE |

### 창작자 수익과 정산 정책

- 플랫폼 수수료는 콘텐츠 판매가의 20%, 창작자 수익은 80%다.
- 플랫폼 수수료는 `floor(판매가 × 20 / 100)`으로 계산하고, 창작자 수익은 `판매가 - 플랫폼 수수료`로 계산한다.
- 콘텐츠 구매가 완료되는 트랜잭션 안에서 창작자 `credit_balance`에 수익 크레딧을 즉시 지급한다.
- 구매자에게 `CreditTransaction(PURCHASE)`, 창작자에게 `CreditTransaction(SALE)`을 생성한다.
- `creator_settlement`에는 판매가, 플랫폼 수수료, 창작자 수익을 기록하고 즉시 `SETTLED` 상태와 `settled_at`을 설정한다.
- 창작자 대시보드의 총매출·수익·판매량은 `creator_settlement`을 기준으로 집계한다.
- 크레딧의 현금 출금과 별도 정산 대기·승인 절차는 MVP 범위에서 제외한다.

### 콘텐츠 상태와 구매 후 권한

- Asset 상태는 `PUBLISHED`, `DELETED`를 사용한다.
- 삭제는 소프트 삭제하며 `status=DELETED`와 `deleted_at`을 기록한다.
- 삭제된 콘텐츠는 공개 목록·상세에서 노출하지 않고 신규 구매를 허용하지 않는다.
- 삭제 전에 구매한 사용자는 보관함에서 콘텐츠와 구매 당시 라이선스를 조회하고 원본을 계속 다운로드할 수 있다.
- 구매 시점의 가격과 라이선스를 `purchase`에 스냅샷으로 저장한다.
- 한 콘텐츠는 하나의 가격과 하나의 라이선스만 가진다.
- 라이선스는 `PERSONAL`, `COMMERCIAL`을 사용한다.
- `PERSONAL`은 비상업적 이용, `COMMERCIAL`은 상업적 이용을 허용하며 두 라이선스 모두 원본 파일 자체의 재판매·재배포는 허용하지 않는다.
- 최초 판매 이후에는 `original_object_key`, 원본 파일 메타데이터, `license_type`을 변경할 수 없다.
- 제목, 설명, 카테고리, 태그, 가격, 미리보기는 판매 이후에도 수정할 수 있다.

### 카테고리 정책

- 카테고리는 별도 `category` 테이블로 관리하고 Asset은 `category_id` 외래 키로 참조한다.
- `category.name`은 Java enum 타입으로 정의하고 JPA `EnumType.STRING`으로 DB의 `varchar` 컬럼에 enum 상수명을 저장한다.
- enum의 순서가 바뀌어도 기존 데이터가 훼손되지 않도록 ordinal 저장은 사용하지 않는다.
- 초기 카테고리는 Flyway seed 데이터로 등록한다.
- MVP에서는 `GET /api/categories` 조회 API만 제공하며 카테고리 관리 API는 제공하지 않는다.
- 카테고리를 운영에서 제거할 때 행을 삭제하지 않고 `active=false`로 변경한다.
- 비활성 카테고리는 신규 콘텐츠 등록 선택지에서 제외하지만 기존 콘텐츠 조회에는 유지한다.
- 카테고리를 추가하거나 이름을 변경하려면 Java enum과 Flyway 마이그레이션을 함께 변경해야 한다.

초기 `CategoryName`과 Flyway seed:

| name         | slug         | display_order | active |
| ------------ | ------------ | ------------- | ------ |
| `NATURE`     | `nature`     | 1             | true   |
| `PEOPLE`     | `people`     | 2             | true   |
| `BUSINESS`   | `business`   | 3             | true   |
| `TECHNOLOGY` | `technology` | 4             | true   |
| `FANTASY`    | `fantasy`    | 5             | true   |
| `ABSTRACT`   | `abstract`   | 6             | true   |
| `LIFESTYLE`  | `lifestyle`  | 7             | true   |
| `OTHER`      | `other`      | 8             | true   |

### 태그 정책

- 콘텐츠 등록·수정 요청은 태그 이름 배열을 전달하며 콘텐츠당 최대 5개를 허용한다.
- 태그 이름은 1자 이상 30자 이하로 제한한다.
- 저장 전에 앞뒤 공백을 제거하고 연속 공백을 하나로 축소하며 영문은 소문자로 정규화한다.
- 정규화한 이름이 같은 태그는 기존 `tag`를 재사용하고, 존재하지 않으면 서버가 생성한다.
- `asset_tag(asset_id, tag_id)` 유니크 제약으로 같은 콘텐츠의 중복 태그 연결을 방지한다.
- 콘텐츠에서 더 이상 사용되지 않는 태그도 자동 삭제하지 않는다.
- `GET /api/tags`는 `query` 검색과 사용량순 정렬을 지원하며 기본 최대 50개를 반환한다.

---

## DB 스키마

실제 DB 스키마와 기준 데이터의 단일 진실 소스는 Flyway 마이그레이션이다.

- 테이블·컬럼·PK·FK·유니크·CHECK·인덱스: [`V1__create_initial_schema.sql`](../backend/src/main/resources/db/migration/V1__create_initial_schema.sql)
- 카테고리·크레딧 상품 기준 데이터: [`V2__seed_reference_data.sql`](../backend/src/main/resources/db/migration/V2__seed_reference_data.sql)
- 적용된 마이그레이션 파일은 수정하지 않고, 스키마 변경 시 새 버전 파일을 추가한다.

---

## API 명세 (구현 예정)

### 공통 응답 규격

- 컨트롤러는 Spring 내부 `Page`, Entity, 예외 객체를 API 응답으로 직접 노출하지 않는다.
- 단건 성공 응답은 `{ "data": { ... } }` 형식을 사용한다.
- 목록 성공 응답은 `{ "data": [...], "page": { ... } }` 형식을 사용한다.
- `page`는 `number`, `size`, `totalElements`, `totalPages`, `hasNext`를 포함한다.
- 페이지 번호는 0부터 시작하며 기본 크기는 20, 최대 크기는 100이다.
- 정렬 필드는 API별 허용 목록으로 제한하고, 허용되지 않은 필드는 `400 Bad Request`로 처리한다.
- 오류 응답은 `code`, `message`, `fieldErrors`, `requestId`를 포함한다.
- `fieldErrors`가 없는 오류는 빈 배열을 반환한다.
- 요청의 유효한 `X-Request-Id`는 그대로 사용하고, 없거나 유효하지 않으면 UUID를 생성한다. 동일한 값은 응답 `X-Request-Id` 헤더, 오류 본문 `requestId`, 로그 MDC에 사용한다.
- 인증 실패는 `401`, 권한 부족은 `403`, 리소스 없음은 `404`, 중복·상태 충돌은 `409`를 사용한다.
- Controller는 항상 `ApiResponse<T>` 또는 `PageResponse<T>`를 반환 타입으로 선언한다. `ResponseEntity`로 감싸지 않는다. `200`이 아닌 성공 상태 코드(예: 등록 `201 Created`)가 필요하면 메서드에 `@ResponseStatus(HttpStatus.CREATED)`를 붙인다.

단건 성공 예시:

```json
{
  "data": {
    "id": 1
  }
}
```

목록 성공 예시:

```json
{
  "data": [],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false
  }
}
```

오류 예시:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "요청 값이 올바르지 않습니다.",
  "fieldErrors": [],
  "requestId": "01J..."
}
```

### Auth

| Method | Path                 | 인증          | 설명                                     |
| ------ | -------------------- | ------------- | ---------------------------------------- |
| POST   | `/api/auth/register` | 불필요        | 회원가입                                 |
| POST   | `/api/auth/login`    | 불필요        | 로그인 → JWT 반환                        |
| POST   | `/api/auth/reissue`  | Refresh token 쿠키 | Access token 재발급 및 Refresh token 회전 |
| POST   | `/api/auth/logout`   | Refresh token 쿠키 | Refresh token 폐기 및 쿠키 제거           |
| GET    | `/api/auth/me`       | Access token  | 현재 사용자 정보                         |
| PUT    | `/api/auth/me`       | Access token  | 사용자 정보 수정                         |

**`POST /api/auth/register` 요청**

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "홍길동"
}
```

응답 (`201 Created`):

```json
{
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "홍길동",
    "role": "USER",
    "creditBalance": 0,
    "createdAt": "2026-07-06T00:00:00.000000"
  }
}
```

**`POST /api/auth/login` 요청**

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

응답: Access token은 본문, Refresh token은 `Set-Cookie`(`HttpOnly`, `Secure`) 헤더로 전달한다.

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "홍길동",
      "role": "USER",
      "creditBalance": 0
    }
  }
}
```

**`POST /api/auth/reissue` 요청**: 요청 본문 없음 — Refresh token 쿠키만 사용한다.

응답: 새 Access token을 본문으로, 회전된 Refresh token을 `Set-Cookie`로 전달한다.

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

**`POST /api/auth/logout`**: 요청 본문 없음. 응답은 `204 No Content`이며 Refresh token 쿠키를 만료시켜 제거한다.

**`GET /api/auth/me` 응답**

```json
{
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "홍길동",
    "role": "USER",
    "status": "ACTIVE",
    "profileUrl": null,
    "introduction": null,
    "creditBalance": 500,
    "createdAt": "2026-07-06T00:00:00.000000"
  }
}
```

**`PUT /api/auth/me` 요청** (전달한 필드만 갱신, `email`·`password`·`role`·`creditBalance`는 이 API로 변경 불가)

```json
{
  "nickname": "새닉네임",
  "profileUrl": "https://cdn.example.com/profile.jpg",
  "introduction": "AI 창작자입니다."
}
```

응답: `GET /api/auth/me`와 동일한 shape.

### Asset (Content)

| Method | Path                 | 인증         | 설명                    |
| ------ | -------------------- | ------------ | ----------------------- |
| GET    | `/api/contents`      | 불필요       | 목록 (페이지·필터·정렬) |
| GET    | `/api/contents/{id}` | 불필요       | 상세                    |
| POST   | `/api/contents`      | Access token | 등록 (창작자)           |
| PUT    | `/api/contents/{id}` | Access token | 수정 (작성자만)         |
| DELETE | `/api/contents/{id}` | Access token | 소프트 삭제 (작성자만)  |

**`GET /api/contents` 쿼리 파라미터**

| 파라미터 | 설명 |
| --- | --- |
| `search` | 제목/설명/태그 검색어 |
| `type` | `image`\|`video`\|`music` |
| `categoryId` | 카테고리 필터 |
| `tag` | 태그 필터 |
| `minPrice` | 최소 가격 |
| `maxPrice` | 최대 가격 |
| `creatorId` | 특정 창작자의 콘텐츠만 조회 (창작자 대시보드/마이페이지용) |
| `sort` | `LATEST`(기본) \| `POPULAR` \| `PRICE_ASC` \| `PRICE_DESC` |

**검색/정렬 정책:** 목록 조회는 `Querydsl` 동적 쿼리로 조건을 조합한다. `LATEST`는 `createdAt desc`, `POPULAR`는 누적 구매 횟수 내림차순 후 최신순, `PRICE_ASC`/`PRICE_DESC`는 가격 정렬 후 최신순으로 처리한다. 검색은 MVP에서 MySQL `LIKE` 기반으로 제목·설명·태그를 대상으로 하며 Elasticsearch나 full-text search는 사용하지 않는다.

**조회수 정책:** `GET /api/contents/{id}` 호출마다 `view_count`를 1 증가시킨다 (사용자/세션당 중복 방지 없음 — 단순 카운터).

**좋아요(likes) 미구현:** 프론트 UI에 표시 필드는 있으나 "좋아요 누르기" 상호작용이 없어 `asset`에 `like_count` 컬럼을 추가하지 않는다. 프론트의 좋아요 표시는 추후 정리 대상 (`.harness/BACKLOG.md` 참조).

**`GET /api/contents` 응답** — 목록 DTO는 페이징을 보존하도록 `XToOne` 정보만 포함하며 태그 컬렉션은 상세 응답에서 제공한다. 원본 파일 정보(`originalObjectKey` 등)는 노출하지 않는다.

```json
{
  "data": [
    {
      "id": 1,
      "title": "노을 지는 도시",
      "description": "사이버펑크 스타일 도시 야경",
      "assetType": "IMAGE",
      "categoryId": 4,
      "previewUrl": "https://cdn.example.com/preview/asset-1.jpg",
      "priceCredit": 120,
      "aiTool": "Midjourney",
      "licenseType": "COMMERCIAL",
      "viewCount": 37,
      "creatorId": 5,
      "creatorNickname": "홍길동",
      "createdAt": "2026-07-06T00:00:00.000000"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

**`GET /api/contents/{id}` 응답** — 목록 항목과 동일한 필드에 `description` 전문을 포함한다.

```json
{
  "data": {
    "id": 1,
    "title": "노을 지는 도시",
    "description": "사이버펑크 스타일 도시 야경. 4K 해상도, 상업적 이용 가능.",
    "assetType": "IMAGE",
    "categoryId": 4,
    "previewUrl": "https://cdn.example.com/preview/asset-1.jpg",
    "priceCredit": 120,
    "aiTool": "Midjourney",
    "licenseType": "COMMERCIAL",
    "viewCount": 38,
    "creatorId": 5,
    "creatorNickname": "홍길동",
    "tags": ["cyberpunk", "city", "sunset"],
    "createdAt": "2026-07-06T00:00:00.000000",
    "updatedAt": null
  }
}
```

**`POST /api/contents` 요청** — `previewObjectKey`·`originalObjectKey`는 `POST /api/files/upload`로 먼저 발급받은 임시 객체 키다. 등록 시점에 서버가 `HEAD` 요청으로 재검증한다.

```json
{
  "title": "노을 지는 도시",
  "description": "사이버펑크 스타일 도시 야경",
  "assetType": "IMAGE",
  "categoryId": 4,
  "previewObjectKey": "tmp/user-5/8f3a.../preview.jpg",
  "originalObjectKey": "tmp/user-5/8f3a.../original.png",
  "originalFilename": "sunset-city.png",
  "contentType": "image/png",
  "fileSize": 4200000,
  "priceCredit": 120,
  "aiTool": "Midjourney",
  "licenseType": "COMMERCIAL",
  "tags": ["cyberpunk", "city", "sunset"]
}
```

응답 (`201 Created`): `GET /api/contents/{id}`와 동일한 shape.

**`PUT /api/contents/{id}` 요청** — 전달한 필드만 갱신한다. `originalObjectKey`·`originalFilename`·`contentType`·`fileSize`·`licenseType`은 최초 판매 이후에는 요청에 포함되어도 거부(`409`)한다.

```json
{
  "title": "노을 지는 도시 (수정)",
  "description": "설명 보완",
  "categoryId": 4,
  "previewObjectKey": "tmp/user-5/9c1b.../preview.jpg",
  "priceCredit": 150,
  "tags": ["cyberpunk", "city", "night"]
}
```

응답: `GET /api/contents/{id}`와 동일한 shape.

**`DELETE /api/contents/{id}`**: 요청 본문 없음. 응답은 `204 No Content` (소프트 삭제 — `status=DELETED`, `deleted_at` 기록).

### Category / Tag

| Method | Path              | 인증   | 설명          |
| ------ | ----------------- | ------ | ------------- |
| GET    | `/api/categories` | 불필요 | 카테고리 목록 |
| GET    | `/api/tags`       | 불필요 | 태그 검색·사용량순 목록 (최대 50개) |

**`GET /api/categories` 응답** — 비활성(`active=false`) 카테고리는 제외한다.

```json
{
  "data": [
    { "id": 1, "name": "NATURE", "slug": "nature", "displayOrder": 1 },
    { "id": 2, "name": "PEOPLE", "slug": "people", "displayOrder": 2 }
  ]
}
```

**`GET /api/tags` 쿼리 파라미터**

| 파라미터 | 설명 |
| --- | --- |
| `query` | 태그 이름 검색어 (부분 일치, 선택) |
| `limit` | 반환 개수, 기본 20·최대 50 |

응답: 사용량(`asset_tag` 연결 수) 내림차순 정렬.

```json
{
  "data": [
    { "id": 3, "name": "cyberpunk", "usageCount": 12 },
    { "id": 7, "name": "city", "usageCount": 9 }
  ]
}
```

### Credit / Payment

| Method | Path                       | 인증         | 설명             |
| ------ | -------------------------- | ------------ | ---------------- |
| GET    | `/api/credit-products`     | 불필요       | 충전 상품 목록   |
| POST   | `/api/payments`            | Access token | 결제 요청 (목업) |
| GET    | `/api/credit-transactions` | Access token | 크레딧 거래 이력 |

**`GET /api/credit-products` 응답** — `ACTIVE` 상품만 `display_order` 오름차순으로 반환한다.

```json
{
  "data": [
    { "id": 1, "code": "BASIC", "name": "Basic", "creditAmount": 500, "bonusCredit": 0, "price": 5000, "displayOrder": 1 },
    { "id": 2, "code": "PLUS", "name": "Plus", "creditAmount": 1000, "bonusCredit": 100, "price": 10000, "displayOrder": 2 },
    { "id": 3, "code": "PRO", "name": "Pro", "creditAmount": 3000, "bonusCredit": 500, "price": 30000, "displayOrder": 3 }
  ]
}
```

**`POST /api/payments` 요청** — `Idempotency-Key` 헤더 필수. 금액·크레딧은 서버가 `creditProductId`로 조회해 결정하며 클라이언트가 전달하지 않는다.

헤더: `Idempotency-Key: 6e2f...`

```json
{
  "creditProductId": 2
}
```

응답 (`201 Created`):

```json
{
  "data": {
    "paymentId": 10,
    "creditProductId": 2,
    "amount": 10000,
    "method": "MOCK",
    "status": "SUCCESS",
    "grantedCredit": 1100,
    "creditBalance": 1100,
    "paidAt": "2026-07-06T00:00:00.000000"
  }
}
```

**`GET /api/credit-transactions` 쿼리 파라미터**

| 파라미터 | 설명 |
| --- | --- |
| `type` | `CHARGE`\|`PURCHASE`\|`SALE` (선택, 미지정 시 전체) |
| `page`, `size` | 공통 페이지네이션 |

응답:

```json
{
  "data": [
    { "id": 21, "type": "CHARGE", "amount": 1100, "balanceAfter": 1100, "reason": "credit_product:PLUS", "createdAt": "2026-07-06T00:00:00.000000" }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1, "hasNext": false }
}
```

### Purchase / Library

| Method | Path             | 인증         | 설명          |
| ------ | ---------------- | ------------ | ------------- |
| POST   | `/api/purchases` | Access token | 콘텐츠 구매   |
| GET    | `/api/library`   | Access token | 구매 보관함   |
| POST   | `/api/downloads` | Access token | 구매 확인, 다운로드 기록 및 원본 Presigned URL 발급 |

**`POST /api/purchases` 요청** — `Idempotency-Key` 헤더 필수. 본인 콘텐츠 구매·중복 구매·잔액 부족은 모두 `409 Conflict`와 구분되는 오류 `code`로 거부한다.

헤더: `Idempotency-Key: a91c...`

```json
{
  "assetId": 1
}
```

응답 (`201 Created`):

```json
{
  "data": {
    "purchaseId": 55,
    "assetId": 1,
    "purchasePriceCredit": 120,
    "licenseType": "COMMERCIAL",
    "creditBalance": 980,
    "purchasedAt": "2026-07-06T00:00:00.000000"
  }
}
```

**`GET /api/library` 쿼리 파라미터**

| 파라미터 | 설명 |
| --- | --- |
| `page`, `size` | 공통 페이지네이션 (기본 `purchased_at` 내림차순) |

응답 — 삭제된 콘텐츠도 구매 당시 정보로 계속 노출한다.

```json
{
  "data": [
    {
      "purchaseId": 55,
      "asset": {
        "id": 1,
        "title": "노을 지는 도시",
        "previewUrl": "https://cdn.example.com/preview/asset-1.jpg",
        "assetType": "IMAGE",
        "deleted": false
      },
      "purchasePriceCredit": 120,
      "licenseType": "COMMERCIAL",
      "purchasedAt": "2026-07-06T00:00:00.000000"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1, "hasNext": false }
}
```

**`POST /api/downloads` 요청** — 구매 여부를 확인한 뒤 다운로드 이력을 남기고 짧은 유효기간의 원본 Presigned URL을 발급한다.

```json
{
  "assetId": 1
}
```

응답:

```json
{
  "data": {
    "downloadUrl": "https://s3.amazonaws.com/aiverse/original/asset-1.png?X-Amz-Signature=...",
    "expiresAt": "2026-07-06T00:05:00.000000"
  }
}
```

### Dashboard

| Method | Path                   | 인증         | 설명             |
| ------ | ---------------------- | ------------ | ---------------- |
| GET    | `/api/dashboard/sales` | Access token | 창작자 판매 통계 |

**쿼리 파라미터**

| 파라미터 | 설명 |
| --- | --- |
| `period` | `7D` \| `30D`(기본) \| `ALL` |

**응답 형태:** 인증된 사용자가 창작자로서 등록한 콘텐츠 기준 총계와 콘텐츠별 판매 목록, 일자별 매출 추이를 함께 반환한다.

```json
{
  "data": {
    "totals": {
      "assetCount": 0,
      "totalSales": 0,
      "totalRevenueCredit": 0
    },
    "series": [
      { "date": "2026-07-06", "salesCount": 0, "revenueCredit": 0 }
    ],
    "items": [
      { "assetId": 1, "title": "", "salesCount": 0, "revenueCredit": 0 }
    ]
  }
}
```

`totals`는 등록 콘텐츠 수·누적 판매 횟수·누적 판매 크레딧, `series`는 선택한 기간의 일자별 판매 횟수·매출, `items`는 콘텐츠별 판매 횟수·매출을 담는다. `salesCount`/`revenueCredit`은 `purchase`(및 `creator_settlement`)에서 집계하며 `asset` 테이블에 비정규화 저장하지 않는다. `items`는 판매량 기준 상위 5개를 우선 반환하고 동률이면 최신 콘텐츠를 우선한다.

### File

| Method | Path                | 인증         | 설명        |
| ------ | ------------------- | ------------ | ----------- |
| POST   | `/api/files/upload` | Access token | 업로드용 Presigned URL 발급 |

**`POST /api/files/upload` 요청** — `purpose`에 따라 "파일 저장과 접근 정책"의 형식·크기 제한을 검증한 뒤 인증 사용자별 임시 경로에 업로드 URL을 발급한다.

```json
{
  "purpose": "ORIGINAL",
  "assetType": "IMAGE",
  "fileName": "sunset-city.png",
  "contentType": "image/png",
  "fileSize": 4200000
}
```

| 필드 | 설명 |
| --- | --- |
| `purpose` | `COVER`(미리보기) \| `ORIGINAL`(원본) |
| `assetType` | `IMAGE`\|`VIDEO`\|`MUSIC` — 원본 크기 제한 판단에 사용, `purpose=COVER`면 무시 |
| `fileName` | 원본 파일명 (확장자 포함) |
| `contentType` | MIME type |
| `fileSize` | byte 단위 크기 |

응답 — 프론트엔드는 `uploadUrl`로 스토리지에 직접 `PUT` 업로드하고, 이후 `POST /api/contents`에 `objectKey`를 전달한다.

```json
{
  "data": {
    "objectKey": "tmp/user-5/8f3a1c2e/sunset-city.png",
    "uploadUrl": "https://s3.amazonaws.com/aiverse/tmp/user-5/8f3a1c2e/sunset-city.png?X-Amz-Signature=...",
    "expiresAt": "2026-07-06T00:10:00.000000"
  }
}
```

---

## Frontend ↔ Backend 연동 상태

| 영역          | 현재                                          | 목표                 |
| ------------- | --------------------------------------------- | -------------------- |
| Frontend API  | VibeX SDK                                     | Spring Boot REST API |
| Frontend 배포 | VibeX 도메인                                  | Vercel               |
| Backend       | Spring Boot 초기 프로젝트 (Entity·API 미구현) | Entity·API 구현      |
