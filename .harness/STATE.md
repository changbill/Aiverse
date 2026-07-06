# STATE — 진행 상황 스냅샷

> 세션을 시작하는 도구(Claude Code / Codex / Cursor)는 이 문서를 먼저 읽고 현재까지 무엇이 완료되었는지 파악한다.
> 작업을 완료할 때마다 이 문서를 갱신한다. 오래된 스냅샷을 지우지 말고 "마지막 갱신"만 갱신한다.

**마지막 갱신:** 2026-07-06

## 완료된 작업

- 2단계 회원과 인증 (이슈 11) 입력 검증·중복·비활성 계정·인증 오류 테스트 커버리지 점검 및 보강. 새 프로덕션 코드 없이 기존 테스트를 감사해 빠진 케이스만 추가: 회원가입 이메일/비밀번호/닉네임 필수값·비밀번호 최대 길이(65자)·닉네임 최소/최대 길이(1자/21자) 400 케이스, 로그인 이메일/비밀번호 필수값·이메일 형식 400 케이스, 닉네임 앞뒤 공백 제거 검증을 추가. 중복 이메일·닉네임(409), 비활성 계정 로그인 거부, 각종 401(인증 실패·토큰 없음·유효하지 않은 토큰·만료/폐기된 refresh 토큰)은 이미 이슈 8~10에서 충분히 커버되어 있어 그대로 유지 — **2단계 회원과 인증 전체 완료**
- 2단계 회원과 인증 (이슈 10) Refresh token 쿠키·해시 저장·회전·로그아웃 구현. `security/RefreshTokenGenerator`(SecureRandom 원문 생성 + SHA-256 해시), `util/RefreshTokenCookieSupport`(HttpOnly·Secure·SameSite=None·Path=/api/auth 쿠키 설정/제거)를 신설. `AuthService.login`이 이제 Access token과 함께 Refresh token을 발급해 DB에는 해시만 저장하고, 원문은 응답 쿠키로만 전달(`LoginResult`). `POST /api/auth/reissue`(기존 토큰 폐기+새 토큰 발급으로 회전), `POST /api/auth/logout`(해당 세션만 폐기, 204) 엔드포인트 추가, `SecurityConfig`에 두 경로를 `permitAll`로 등록(Refresh token 쿠키로 자체 인증하므로 Access token 필터 대상이 아님). 실제 로그인→재발급→회전된 토큰 재사용 차단→로그아웃 흐름을 검증하는 통합 테스트를 `SecurityFilterChainTest`에 추가. 이번 이슈부터 서브에이전트 없이 직접 구현
- 2단계 회원과 인증 (이슈 9) `SecurityFilterChain` 구성 완료: `JwtAuthenticationFilter`(`OncePerRequestFilter`, `BearerTokenExtractor`+`JwtTokenProvider` 재사용)가 유효한 토큰이면 `SecurityContext`에 인증 정보를 설정하고, 유효하지 않은 토큰은 `RestAuthenticationEntryPoint`로 즉시 401(`INVALID_TOKEN`) 응답, 헤더 없음은 Security 인가 단계가 401(`AUTHENTICATION_REQUIRED`)로 처리. `POST /api/auth/{register,login}`과 Swagger 경로만 `permitAll`, 나머지는 인증 필요. `AuthController.me`는 `@AuthenticationPrincipal Long userId`로 단순화(수동 헤더 파싱 제거), `AuthService.getCurrentUser`도 `Long userId`를 직접 받도록 변경. `RequestIdFilter`에 `@Order(HIGHEST_PRECEDENCE)`를 추가해 Security 필터보다 먼저 실행되도록 수정(401 응답에도 `requestId`가 정확히 반영됨). 실제 필터 체인을 검증하는 통합 테스트(`SecurityFilterChainTest`, `IntegrationTestSupport`+`@AutoConfigureMockMvc`) 추가. 진행 중 `springdoc-openapi-starter-webmvc-ui:2.8.5`가 Spring Data 4.1(`TypeInformation` 패키지 이동)과 호환되지 않아 컨텍스트 기동이 실패하는 문제를 발견해 `3.0.3`으로 업그레이드해 해결
- Controller 리팩터 2건: (1) `AuthController`의 Bearer 토큰 파싱을 `util/BearerTokenExtractor`(static, 단위 테스트 포함)로 분리해 Controller가 라우팅만 담당하도록 정리 (2) `register`가 쓰던 `ResponseEntity<ApiResponse<T>>`를 다른 엔드포인트와 동일하게 `ApiResponse<T>` 반환 + `@ResponseStatus(CREATED)`로 통일. 두 컨벤션 모두 `.claude/agents/api-builder.md` 작업 원칙과 `.harness/ARCHITECTURE.md`/`DECISIONS.md`에 반영
- 백엔드에 `springdoc-openapi-starter-webmvc-ui`로 Swagger UI(`/swagger-ui.html`, `/v3/api-docs`) 도입, `config/OpenApiConfig`에 JWT Bearer 보안 스키마(`bearer-jwt`) 등록 — 이후 Controller마다 `@Tag`/`@Operation`/`@SecurityRequirement` 부여
- 2단계 회원과 인증 (이슈 8) 회원가입·로그인·현재 사용자 조회(`POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`)를 TDD로 구현. `JwtTokenProvider`(HMAC-SHA256, jjwt)로 Access token 발급/검증, `AuthErrorCode`(중복 이메일·닉네임, 인증 실패 등) 추가. `GET /api/auth/me`는 Security 필터가 아직 없어 컨트롤러가 `Authorization` 헤더를 직접 파싱하는 임시 구현 — 다음 이슈(Security 필터)에서 대체 예정. 테스트 25개(Service 10·Controller 11·JwtTokenProvider 4) 통과
- 2단계 회원과 인증 (이슈 7) `User`·`RefreshToken` Entity를 Flyway V1 스키마에 맞춰 구현하고, Repository를 도메인 인터페이스/`jpa`/`impl` 3계층으로 TDD 구현 (`aiverse-backend-builder` 스킬: backend-architect → api-builder). `UserRepositoryTest`·`RefreshTokenRepositoryTest`가 Testcontainers MySQL로 각각 통과 확인
- `ARCHITECTURE.md`의 API 명세에 엔드포인트별 요청 파라미터/본문과 응답 JSON 예시 추가 (Auth, Asset/Content, Category/Tag, Credit/Payment, Purchase/Library, File) — 기존에는 Dashboard만 예시가 있었음
- 2단계 착수 전 컨벤션 확정: Repository를 도메인 인터페이스(`repository/*.java`)/JPA 실제 구현체(`repository/jpa/*.java`)/중간 구현체(`repository/impl/*.java`) 3계층으로 구성하고, 이후 모든 백엔드 구현은 TDD(테스트 우선)로 진행. 도구 중립 문서(`.harness/ARCHITECTURE.md`·`DECISIONS.md`)에 반영해 Codex/Cursor에도 적용되며, Claude Code 전용 `.claude/agents/{backend-architect,api-builder,qa}.md`·`.claude/skills/aiverse-backend-builder/SKILL.md`도 동기화
- `ARCHITECTURE.md`의 테이블별 컬럼 표와 참고 DDL을 제거하고 Flyway V1·V2를 DB 스키마·기준 데이터의 단일 진실 소스로 연결
- 1단계 백엔드 공통 기반 (이슈 6) `IntegrationTestSupport` 공통 베이스(`@Testcontainers` + `MySQLContainer` + `@ServiceConnection` + `@ActiveProfiles("test")`)를 추가하고 `AiverseApplicationTests`가 이를 상속하도록 변경. `spring-boot-testcontainers` 의존성 보완. 실제 실행으로 Flyway V1·V2가 컨테이너 MySQL에 적용되고 애플리케이션 컨텍스트가 정상 기동함을 확인, 전체 테스트 11개 통과 — 1단계 백엔드 공통 기반 전체 완료
- 1단계 백엔드 공통 기반 (이슈 5) Flyway `V1`에 12개 테이블·PK·FK·유니크·CHECK·조회 인덱스, `V2`에 카테고리 8종·크레딧 상품 3종 seed 구현. Spring Boot 4 Flyway starter 보완 후 MySQL 8.0 실제 적용, 재실행 및 전체 테스트 11개 통과
- 1단계 백엔드 공통 기반 (이슈 4) 단건 `{data}`·목록 `{data,page}` 응답 DTO, 공통 오류 코드·애플리케이션 예외·전역 예외 처리, `X-Request-Id` 응답 헤더·오류 본문·MDC 전파 구현 및 MockMvc 계약 테스트 10개 통과
- `backend/.env`에 Docker Compose용 MySQL·MinIO 로컬 환경 변수 구성, 루트 `.gitignore`의 기존 `.env` 규칙으로 Git 제외 확인
- 1단계 백엔드 공통 기반 (이슈 1) Gradle에 Security, Validation, JPA, Querydsl, Flyway, MySQL, JWT, S3 SDK, Testcontainers 의존성 구성
- 1단계 백엔드 공통 기반 (이슈 2) `local`·`test`·`prod` 프로필과 환경 변수 바인딩 구성
- 1단계 백엔드 공통 기반 (이슈 3) `backend/docker-compose.yml`로 MySQL 8.0·MinIO 로컬 실행 환경 구성 (MinIO는 `minio-init` 서비스로 `aiverse-local` 버킷 자동 생성). `application-local.yaml` 기본값과 일치 확인, MySQL 접속과 MinIO 헬스체크 실제 기동으로 검증 완료
- `.harness/DECISIONS.md`를 최신순으로 정렬하고 세 도구 지침에 최상단 추가 규칙 반영
- 하네스의 `.harness/PLAN.md`에서 완료된 계획을 제거하도록 문서 역할을 정리하고 세 도구 지침 동기화
- API 명세의 모든 엔드포인트에 `불필요`·`Access token`·`Refresh token` 인증 조건 명시
- 프론트엔드(React 18 + Vite + TailwindCSS), 백엔드(Java 21 + Spring Boot + Gradle) 모노레포 분리
- 서비스 기획(README.md) 및 프로젝트 전체 명세를 `.harness/ARCHITECTURE.md`로 통합 (기존 3개 SPEC.md 삭제)
- 크로스 툴(Claude Code/Codex/Cursor) 작업 연속성 하네스 구성 (`.harness/`, `CLAUDE.md`, `AGENTS.md`, `.cursor/rules/harness.mdc`)
- AIverse 백엔드 구현을 위한 Claude Code 전용 에이전트 팀 스캐폴딩 완료: `backend-architect`/`api-builder`/`qa` 3인 팀 + `aiverse-backend-builder` 오케스트레이터 (아직 실제 모듈 구현은 미실행)
- 프론트엔드 전체 코드 분석 후 API 명세 갭 보완: `asset.view_count` 추가, 조회수 증가 정책, `GET /api/contents`의 `creatorId` 필터, `GET /api/dashboard/sales` 응답 shape(`totals`+`items`) 확정

## 진행 중인 작업

_(없음 — 2단계 회원과 인증 이슈 7~11 전체 완료, 3단계 카테고리·태그·콘텐츠 탐색 착수 대기)_

## 다음으로 예정된 작업

_(PLAN.md 참조 — 3단계 카테고리·태그·콘텐츠 탐색부터 → ... 9단계 전체 검증과 문서화 순)_
