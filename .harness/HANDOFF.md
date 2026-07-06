# HANDOFF — 세션 인수인계

> 세션을 종료하거나 다른 도구로 전환하기 직전에 이 문서를 갱신한다.
> 다음 세션(다른 도구일 수 있음)은 이 문서를 가장 먼저 읽고 이어서 작업한다.
> 새 항목을 맨 위에 추가하고, 과거 기록은 아래로 밀어 남겨둔다 (이력 추적용).

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 이슈 11 "입력 검증, 중복 이메일·닉네임, 비활성 계정, 인증 오류 테스트"를 진행했다. 새 프로덕션 코드를 추가하지 않고, 기존 테스트(`AuthServiceTest`/`AuthControllerTest`/`SecurityFilterChainTest`)를 감사해 실제로 빠진 케이스만 보강하는 방식으로 접근했다. 회원가입 요청의 이메일/비밀번호/닉네임 필수값 누락, 비밀번호 최대 길이 초과(65자), 닉네임 최소/최대 길이 초과(1자/21자) 400 케이스와 로그인 요청의 이메일/비밀번호 필수값 누락·이메일 형식 오류 400 케이스, 닉네임 앞뒤 공백 제거 검증을 `AuthControllerTest`/`AuthServiceTest`에 추가했다. 중복 이메일·닉네임(409), 비활성 계정 로그인 거부, 각종 401(토큰 없음/유효하지 않음/만료·폐기된 refresh 토큰)은 이슈 8~10에서 이미 충분히 테스트되어 있어 그대로 두었다. 이로써 **2단계 회원과 인증(이슈 7~11)이 모두 완료**됐다.

**막힌 부분:** 없음.

**다음에 할 일:** PLAN.md 3단계 "카테고리·태그·콘텐츠 탐색"을 시작한다 — 첫 항목은 "Category·Tag·Asset·AssetTag 엔티티와 조회 Repository 구현"이다. 이미 확립된 컨벤션(Repository 3계층, TDD, Swagger 애노테이션, `ApiResponse` 고정 반환, Controller는 라우팅만)을 그대로 따르면 된다.

**참고사항:** 이 이슈는 순수 테스트 보강이라 실질적으로 코드 리뷰/커버리지 감사에 가까웠다. 앞으로 비슷한 "테스트 보강" 이슈를 만나면 무작정 새 테스트를 나열하기보다 먼저 기존 테스트 목록을 훑어 진짜 빈 곳만 채우는 이번 접근이 효율적이었다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 이슈 10 "Refresh token 쿠키·해시 저장·회전·로그아웃 구현"을 서브에이전트 없이 직접 TDD로 구현했다. `security/RefreshTokenGenerator`(SecureRandom 32바이트 원문 + SHA-256 해시), `util/RefreshTokenCookieSupport`(HttpOnly·Secure·SameSite=None·Path=/api/auth)를 새로 만들고, `AuthService.login`이 Access token과 함께 Refresh token을 발급하도록 확장했다(`LoginResult` 레코드로 컨트롤러에 원문 토큰 전달, DB에는 해시만 저장). `POST /api/auth/reissue`(기존 토큰 폐기 후 새 토큰 발급 = 회전)와 `POST /api/auth/logout`(해당 세션만 폐기, 204)을 추가하고 `SecurityConfig`에서 이 두 경로를 `permitAll`로 등록했다(Access token이 아니라 Refresh token 쿠키로 자체 인증하는 경로라서). `SecurityFilterChainTest`에 로그인→쿠키 확인→재발급→회전된 토큰 재사용 시 차단→로그아웃 흐름을 실제 필터 체인으로 검증하는 테스트를 추가했다.

**막힌 부분:** 중간에 Lombok `@RequiredArgsConstructor`가 `@Value` 애노테이션을 생성자 파라미터로 복사하지 못해 `AuthController`에서 `No qualifying bean of type 'long'` 오류가 났다 — `@RequiredArgsConstructor` 대신 명시적 생성자로 바꿔 해결했다(이슈 8의 `AuthService`에서도 이미 같은 이유로 명시적 생성자를 썼었다).

**다음에 할 일:** PLAN.md 2단계의 마지막 항목 "입력 검증, 중복 이메일·닉네임, 비활성 계정, 인증 오류 테스트"를 진행한다. 다만 이 시나리오 대부분(이메일/비밀번호/닉네임 검증, 중복 이메일·닉네임 409, 비활성 계정 로그인 거부, 각종 401)은 이슈 8·9·10에서 이미 테스트로 커버돼 있으니, 새로 작성하기보다 먼저 `AuthServiceTest`/`AuthControllerTest`/`SecurityFilterChainTest`를 훑어 빠진 케이스만 채우는 커버리지 점검으로 접근하는 게 낫다. 그다음은 3단계 "카테고리·태그·콘텐츠 탐색"이다.

**참고사항:** `Value`(Lombok 자동 생성자에서 `@Value` 필드 애노테이션이 파라미터로 복사되지 않는 문제)는 이 프로젝트에서 두 번째로 겪은 패턴이니, `@Value`가 필요한 필드가 있는 클래스는 처음부터 `@RequiredArgsConstructor` 대신 명시적 생성자를 쓰는 게 낫다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 이슈 9 "Access token 발급과 Security 인증 필터 구현"을 진행했다(이번부터 서브에이전트 없이 직접 구현 — 아래 참고). `JwtAuthenticationFilter`가 유효한 토큰이면 `SecurityContext`에 인증을 설정하고, 유효하지 않은 토큰은 `RestAuthenticationEntryPoint`를 직접 호출해 401(`INVALID_TOKEN`)로 즉시 응답하며, 헤더가 아예 없는 경우는 Security 인가 단계가 자연스럽게 401(`AUTHENTICATION_REQUIRED`)로 처리하도록 설계했다. `SecurityConfig`(`SecurityBeansConfig`에서 이름 변경)에 `SecurityFilterChain`을 구성해 `register`/`login`/Swagger 경로만 `permitAll`, 나머지는 인증 필요로 설정했다. `AuthController.me`/`AuthService.getCurrentUser`를 `@AuthenticationPrincipal Long userId` 기반으로 단순화했다. `RequestIdFilter`에 `@Order(HIGHEST_PRECEDENCE)`를 추가해 Security 필터보다 먼저 실행되도록 고쳤다(안 그러면 Security 단계의 401 응답에 requestId가 안 붙는 문제가 있었다). 실제 필터 체인 동작을 검증하는 `SecurityFilterChainTest`(`@AutoConfigureMockMvc` + 실제 컨텍스트)를 추가했다.

**막힌 부분:** 진행 중 `springdoc-openapi-starter-webmvc-ui:2.8.5`가 Spring Data 4.1과 호환되지 않아(`TypeInformation` 클래스 패키지 이동) 전체 컨텍스트 기동이 실패하는 문제를 만났다 — `3.0.3`으로 올려서 해결했다. 지금은 막힌 것 없음.

**다음에 할 일:** PLAN.md 2단계의 마지막 두 항목 "Refresh token 쿠키·해시 저장·회전·로그아웃 구현"과 "입력 검증, 중복 이메일·닉네임, 비활성 계정, 인증 오류 테스트"를 진행한다.

**참고사항:** 사용자가 "이 프로젝트는 단일 에이전트로 충분하니 서브에이전트를 쓰지 말라"고 명시적으로 요청했다 — `CLAUDE.md`의 "AIverse 백엔드 구현 에이전트 팀" 섹션을 "단일 에이전트" 섹션으로 갱신했다. 이후 세션(어떤 도구든)은 `backend-architect`/`api-builder`/`qa`나 `aiverse-backend-builder` 스킬을 트리거하지 말고 직접 구현할 것. 서브에이전트 호출은 매번 7~14분씩 걸려 이 프로젝트 규모에는 비효율적이었다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 이슈 8 코드에 대한 사용자 피드백 2건을 반영해 컨벤션으로 굳혔다. (1) `AuthController.extractBearerToken`(private 메서드)을 `util/BearerTokenExtractor.extract(...)`(static)로 분리 — Controller는 라우팅만 담당한다는 원칙. (2) `register`가 `ResponseEntity<ApiResponse<RegisterResponse>>`를 반환하던 것을 `login`/`me`와 동일하게 `ApiResponse<T>` 반환 + `@ResponseStatus(HttpStatus.CREATED)`로 통일 — Controller 반환 타입은 항상 `ApiResponse<T>`/`PageResponse<T>`로 고정하고 `ResponseEntity`는 쓰지 않는다. 두 건 모두 `.harness/DECISIONS.md`·`ARCHITECTURE.md`(공통 응답 규격)와 `.claude/agents/api-builder.md` 작업 원칙에 반영해 다음 Controller 작업부터 자동으로 지켜지게 했다.

**막힌 부분:** 없음.

**다음에 할 일:** PLAN.md 2단계의 다음 항목 "Access token 발급과 Security 인증 필터 구현"을 진행한다.

**참고사항:** 두 리팩터 모두 처음엔 실수로 master에 바로 코드를 수정했다가, 브랜치 전략을 뒤늦게 떠올리고 `feature/...` 브랜치를 만들어 옮긴 뒤 커밋·병합했다 — 다음에는 코드를 고치기 전에 먼저 브랜치 상태를 확인할 것.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 사용자 요청으로 Swagger UI(springdoc-openapi) 컨벤션을 먼저 확정(`DECISIONS.md`, `ARCHITECTURE.md`에 반영, `config/OpenApiConfig`로 JWT Bearer 스키마 등록)한 뒤, 이슈 8 "회원가입·로그인·현재 사용자 조회 구현"을 `aiverse-backend-builder` 스킬(api-builder 에이전트)로 진행했다. `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`를 TDD로 구현했고 `AuthController`에 `@Tag`/`@Operation`/`@SecurityRequirement` Swagger 애노테이션을 붙였다. `JwtTokenProvider`(jjwt, HMAC-SHA256)로 Access token을 발급/검증하며, `app.jwt.secret-key`/`app.jwt.access-token-expiration-seconds`를 `application*.yaml`에 추가하고 `JWT_SECRET_KEY` 환경변수를 `ARCHITECTURE.md`에 문서화했다. 테스트 25개(AuthServiceTest 10, AuthControllerTest 11, JwtTokenProviderTest 4) 통과. `feature/8-회원가입-로그인-사용자조회-구현` 브랜치에서 작업 후 master에 병합.

**막힌 부분:** 없음.

**다음에 할 일:** PLAN.md 2단계의 다음 항목 "Access token 발급과 Security 인증 필터 구현"을 진행한다. 이번 이슈에서 `GET /api/auth/me`는 Spring Security `SecurityFilterChain`이 아직 없어 `AuthController`가 `Authorization` 헤더를 직접 파싱해 `AuthService.getCurrentUser(String accessToken)`에 넘기는 임시 구현이다 — 다음 이슈에서 JWT 인증 필터를 추가하면 `AuthController.me`를 `@AuthenticationPrincipal`(또는 유사한 방식)로 바꾸고 `extractBearerToken` 수동 파싱 코드를 제거해야 한다.

**참고사항:** api-builder에게 "설명형/restating 주석 달지 말 것"을 프롬프트에 명시했더니 이번에는 정말 비직관적인 WHY만 남기고 나머지는 깔끔했다(이전 이슈 7 때 리더가 직접 정리해야 했던 것과 대조적). 앞으로 이 스킬을 쓸 때 이 문구를 계속 포함하면 좋다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 2단계 회원과 인증의 이슈 7 "User·RefreshToken 엔티티와 Repository 구현"을 `aiverse-backend-builder` 스킬로 진행했다. 이 세션에는 TeamCreate/TaskCreate 같은 팀 전용 툴이 없어서, Agent 툴로 `backend-architect`(엔티티 설계) → `api-builder`(Repository 3계층, TDD) 순서로 직접 조율했다. `User`/`RefreshToken` Entity를 Flyway V1 스키마에 맞춰 만들고, `UserRepository`/`RefreshTokenRepository`(도메인 인터페이스) + `repository/jpa/*JpaRepository` + `repository/impl/*RepositoryImpl` 3계층을 TDD로 구현, `UserRepositoryTest`/`RefreshTokenRepositoryTest`가 Testcontainers MySQL로 각각 통과했다. `feature/7-유저-리프레시토큰-엔티티-구현` 브랜치에서 작업 후 master에 병합.

**막힌 부분:** 없음(코드 관점). 다만 이 로컬 PC에 다른 프로젝트(funchat, byeoldam 등) 컨테이너가 15개 넘게 떠 있고 Testcontainers `withReuse(true)`가 `~/.testcontainers.properties`(`testcontainers.reuse.enable=true`) 없이는 무시되어, 전체 `gradlew test`를 한꺼번에 돌리면 MySQL 커넥션이 간헐적으로 실패한다(개별 테스트 클래스는 각각 정상 통과). 사용자가 "개별 테스트 통과로 충분, 전체 스위트 재검증은 스킵"으로 확인해줬다.

**다음에 할 일:** PLAN.md 2단계의 다음 항목 "회원가입·로그인·현재 사용자 조회 구현" — `UserRepository`/`RefreshTokenRepository`를 사용하는 Service/Controller/DTO를 `.harness/ARCHITECTURE.md`의 Auth API 명세(요청/응답 shape)에 맞춰 TDD로 구현한다.

**참고사항:** 서브에이전트(backend-architect, api-builder)가 기본적으로 설명형 Javadoc 주석을 과하게 붙이는 경향이 있어 리더가 직접 정리했다 — 다음에 이 스킬을 쓸 때는 프롬프트에 "설명형/restating 주석 달지 말고 진짜 비직관적인 WHY만" 이라고 명시하는 게 낫다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** `.harness/ARCHITECTURE.md`의 "API 명세" 섹션에 Dashboard를 제외한 모든 엔드포인트(Auth 6개, Asset/Content 5개, Category/Tag 2개, Credit/Payment 3개, Purchase/Library 3개, File 1개)의 요청 파라미터·본문과 응답 JSON 예시를 추가했다. 필드명은 기존 결정(camelCase, `view_count`→`viewCount`, `creatorId` 필터 등)과 Flyway `V1` 스키마 컬럼에 맞춰 설계했고, 원본 파일 정보(`originalObjectKey` 등)는 목록/상세 응답에 노출하지 않고 구매 후 `POST /api/downloads`로만 제공되도록 명시했다.

**막힌 부분:** 없음.

**다음에 할 일:** `.harness/PLAN.md`의 2단계 "회원과 인증"을 Repository 3계층 + TDD 컨벤션과 이번에 추가한 Auth API 명세(요청/응답 shape)에 맞춰 진행한다.

**참고사항:** 이번 추가는 아직 구현되지 않은 API의 설계 문서이며 실제 DTO는 아니다 — 2단계 이후 구현 시 이 shape을 기준으로 삼되, 세부 필드가 구현 중 조정되면 이 섹션을 함께 갱신한다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 2단계(회원과 인증) 착수 전, 사용자가 요청한 Repository 계층 구조와 TDD 개발 방법론을 문서화했다. Repository는 도메인 인터페이스(`repository/{Entity}Repository.java`) + Spring Data JPA 실제 구현체(`repository/jpa/{Entity}JpaRepository.java`) + 이를 감싸는 중간 구현체(`repository/impl/{Entity}RepositoryImpl.java`)의 3계층으로 구성하고, Service는 도메인 인터페이스에만 의존하도록 정했다. 이후 모든 백엔드 구현(Repository/Service/Controller)은 TDD(실패하는 테스트 우선 → 최소 구현 → 리팩터링)로 진행하기로 했다. 도구 중립(Codex/Cursor 포함) 문서인 `.harness/ARCHITECTURE.md`("Repository 계층 구조", "개발 방법론 — TDD" 절 신설, 폴더 구조도 최신화)와 `.harness/DECISIONS.md`에 반영했고, Claude Code 전용 `.claude/agents/{backend-architect,api-builder,qa}.md`·`.claude/skills/aiverse-backend-builder/SKILL.md`도 동일 컨벤션으로 동기화했다.

**막힌 부분:** 없음.

**다음에 할 일:** `.harness/PLAN.md`의 2단계 "회원과 인증"부터 이 컨벤션(Repository 3계층 + TDD)을 적용해 진행한다 — User·RefreshToken 엔티티와 Repository 구현이 첫 항목이다.

**참고사항:** 작업 중 다른 세션(Codex로 추정, 커밋 `b17d1a2`/`991d8b8`)이 병행으로 `ARCHITECTURE.md`의 "DB 스키마" 섹션 중복 컬럼 표를 제거한 것을 확인했다 — 내가 수정한 "Backend 명세" 하위 섹션과 겹치지 않아 충돌 없이 반영됨. 문서 전용 변경이라 별도 feature 브랜치 없이 master에 직접 커밋한다 (과거 "docs:" 커밋들과 동일한 관례).

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 이슈 6 "Testcontainers MySQL 기반 통합 테스트 공통 설정 및 애플리케이션 기동 검증"을 구현했다. `src/test/java/com/example/aiverse/support/IntegrationTestSupport.java`에 `@Testcontainers`+`MySQLContainer`(`withReuse(true)`)+`@ServiceConnection`+`@ActiveProfiles("test")` 공통 베이스를 추가하고, `AiverseApplicationTests`가 이를 상속하도록 변경했다. `build.gradle`에 `spring-boot-testcontainers` 의존성을 추가했다. `./gradlew test`를 실제로 실행해 컨텍스트가 정상 기동하고(`contextLoads`), Flyway V1·V2가 컨테이너 MySQL에 실제로 적용되는 것(`--info` 로그로 "Successfully applied 2 migrations ... now at version v2" 확인)을 검증했다. 전체 테스트 11개 통과로 1단계 백엔드 공통 기반(이슈 1~6)이 모두 완료됐다.

**막힌 부분:** 없음.

**다음에 할 일:** `.harness/PLAN.md`의 2단계 "회원과 인증"부터 진행한다 — User·RefreshToken 엔티티와 Repository 구현이 첫 항목이다.

**참고사항:** 브랜치 `feature/6-테스트컨테이너-공통설정-기동검증`에서 작업. PLAN.md에서 1단계 섹션 전체(제목 포함)를 제거했다 — 완료된 계획은 STATE.md에 반영 후 제거한다는 하네스 규칙에 따름.

---

## 2026-07-06 — Codex

**무엇을 했나:** Flyway와 중복되던 `.harness/ARCHITECTURE.md`의 테이블별 컬럼 표와 참고 DDL 블록을 제거했다. `DB 스키마` 섹션은 실제 스키마·인덱스의 단일 진실 소스인 `V1__create_initial_schema.sql`과 기준 데이터의 단일 진실 소스인 `V2__seed_reference_data.sql` 링크 및 새 버전 마이그레이션 추가 원칙만 남겼다.

**막힌 부분:** 없음.

**다음에 할 일:** `.harness/PLAN.md`의 2단계 회원과 인증부터 진행한다.

**참고사항:** 도메인 정책, seed 정책, API 계약은 아키텍처 설명으로 유지했다. 제거한 것은 Flyway SQL과 직접 중복되는 상세 스키마 정의뿐이다.

---

## 2026-07-06 — Codex

**무엇을 했나:** 이슈 5 초기 Flyway 구성을 구현했다. `V1__create_initial_schema.sql`에 12개 도메인 테이블과 PK·FK·유니크·CHECK·조회 인덱스를, `V2__seed_reference_data.sql`에 카테고리 8종과 크레딧 상품 3종을 작성했다. Spring Boot 4에서 Flyway 자동 구성이 빠진 것을 확인해 `spring-boot-starter-flyway`로 의존성을 보완했다. MySQL 8.0에 V1·V2를 실제 적용해 12개 테이블, seed, 핵심 제약을 조회했고 이미 적용된 DB에서 전체 테스트 11개가 다시 통과했다.

**막힌 부분:** 호스트 3306 포트를 다른 프로젝트의 `funchat-local-mysqldb`가 사용 중이라 검증 중에만 AIverse MySQL을 3307로 노출했다. 다른 컨테이너는 건드리지 않았고, 검증 후 AIverse MySQL을 기존처럼 내부 네트워크 전용의 healthy 상태로 복구했다.

**다음에 할 일:** `.harness/PLAN.md` 1단계의 마지막 항목인 Testcontainers MySQL 기반 통합 테스트 공통 설정 및 애플리케이션 기동 검증을 진행한다.

**참고사항:** `payment`·`purchase` 멱등키를 사용자별 복합 유니크로 정의했고, 명세의 타입 불일치였던 `credit_transaction.payment_id`를 BIGINT FK, `payment.paid_at`을 DATETIME(6)으로 정정했다.

---

## 2026-07-06 — Codex

**무엇을 했나:** 이슈 4 공통 API 계층을 구현했다. 단건 `ApiResponse`, 페이지 `PageResponse`·`PageInfo`, 공통 `ErrorCode`·`ApplicationException`, Validation/JSON/HTTP 메서드/미디어 타입/예상 밖 오류를 처리하는 `GlobalExceptionHandler`, `X-Request-Id`를 응답 헤더·오류 본문·MDC로 전파하는 `RequestIdFilter`를 추가했다. `CommonWebContractTest`의 계약 테스트 10개가 통과했다.

**막힌 부분:** 이슈 4 테스트는 통과했다. 전체 `gradlew test`는 기존 `AiverseApplicationTests.contextLoads()`가 DB 연결 정보 없이 Hibernate dialect를 결정하지 못해 1개 실패한다. 계획된 Testcontainers MySQL 공통 설정 전까지 존재하는 선행 상태다.

**다음에 할 일:** `.harness/PLAN.md` 1단계의 다음 항목인 초기 Flyway 스키마·인덱스·카테고리·크레딧 상품 seed 작성을 진행한다.

**참고사항:** 현재 브랜치는 `feature/4-공통-응답-오류처리-구성`이다. 이전 세션의 `.env` 관련 하네스 변경과 `backend/docker-compose.yml` 미커밋 변경은 그대로 보존했다.

---

## 2026-07-06 — Codex

**무엇을 했나:** `backend/docker-compose.yml`이 요구하는 MySQL·MinIO 변수 6개를 `backend/.env`에 작성했다. `docker compose config --quiet`로 변수 치환과 Compose 구성을 검증하고, `git check-ignore -v backend/.env`로 루트 `.gitignore`의 기존 `.env` 규칙이 적용되는 것을 확인했다.

**막힌 부분:** 없음. Docker 사용자 `config.json` 접근 경고가 출력됐지만 Compose 검증은 종료 코드 0으로 통과했다.

**다음에 할 일:** `.harness/PLAN.md`의 1단계 다음 항목인 공통 응답·오류 처리 구성을 진행한다.

**참고사항:** `backend/.env`는 Git에서 제외되므로 로컬에만 존재한다. 기존 `backend/docker-compose.yml` 수정과 `.claude/settings.local.json`은 건드리지 않았다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** PLAN.md 1단계 잔여 항목 중 이슈 3 "Docker Compose MySQL·MinIO 로컬 실행 환경 구성"을 구현. `backend/docker-compose.yml`에 MySQL 8.0, MinIO, 버킷 자동 생성용 `minio-init`(mc) 서비스를 정의했고 값은 `application-local.yaml` 기본값(`aiverse`/`aiverse`/`aiverse`, `minioadmin`/`minioadmin123`, 버킷 `aiverse-local`)과 일치시켰다. 루트/백엔드 `README.md`의 Getting Started에 `docker compose up -d` 안내를 추가했다. Docker Desktop을 직접 기동해 `docker compose up -d`로 실제 실행하고 MySQL 접속(`SELECT 1`), MinIO 헬스체크(`/minio/health/live` 200), 버킷 생성 로그를 확인해 동작을 검증했다.

**막힌 부분:** 없음.

**다음에 할 일:** PLAN.md 1단계의 다음 항목인 "공통 `{data}`·`{data,page}` 응답, 오류 응답, 예외 처리, `requestId` 구성"을 이어서 구현한다.

**참고사항:** 브랜치 `feature/3-도커컴포즈-로컬환경-구성`에서 작업. 애플리케이션 기동 검증(Flyway 스키마·Testcontainers 포함)은 이슈 3의 범위가 아니라 1단계 마지막 체크리스트 항목("Testcontainers MySQL 기반 통합 테스트 공통 설정 및 애플리케이션 기동 검증")에서 다룬다 — 지금은 아직 Entity/마이그레이션이 없어 `bootRun`을 실행하면 Flyway/validate 단계에서 실패한다.

---

## 2026-07-06 — Codex

**무엇을 했나:** `.harness/DECISIONS.md`의 모든 결정을 날짜 내림차순으로 재정렬하고, 새 결정은 표 최상단에 추가하도록 `AGENTS.md`, `CLAUDE.md`, `.cursor/rules/harness.mdc`를 동기화했다.

**막힌 부분:** 없음.

**다음에 할 일:** `AIverse MVP 구현 로드맵`에 대한 사용자 피드백 또는 컨펌을 기다린다.

**참고사항:** 결정 내용은 유지하고 정렬 순서만 변경했다.

---

## 2026-07-06 — Codex

**무엇을 했나:** PLAN의 역할을 사용자 의도에 맞게 정정했다. `.harness/PLAN.md`에는 제안·확정·진행 중인 계획을 유지하고, 완료된 계획만 결과 산출물에 반영한 뒤 제거하도록 `AGENTS.md`, `CLAUDE.md`, `.cursor/rules/harness.mdc`와 하네스 기록을 갱신했다.

**막힌 부분:** 없음.

**다음에 할 일:** `AIverse MVP 구현 로드맵`에 대한 사용자 피드백 또는 컨펌을 기다린다.

**참고사항:** 직전 기록의 “컨펌 전 계획 초안 전용” 해석은 사용자 정정에 따라 폐기했다.

---

## 2026-07-06 — Codex

**무엇을 했나:** `.harness/PLAN.md`에서 합의·완료되어 다른 산출물에 존재하는 항목을 제거하고, 미합의 상태인 `AIverse MVP 구현 로드맵`만 유지했다. `AGENTS.md`, `CLAUDE.md`, `.cursor/rules/harness.mdc`를 갱신해 PLAN을 사용자 컨펌 전 계획 초안 전용으로 정의했다.

**막힌 부분:** 없음.

**다음에 할 일:** 사용자가 `AIverse MVP 구현 로드맵`을 컨펌하면 합의 내용을 해당 산출물에 반영하고 PLAN에서 제거한 뒤 구현을 시작한다.

**참고사항:** 새 워크플로우 결정은 `.harness/DECISIONS.md`, 완료 상태는 `.harness/STATE.md`에 반영했다.

---

## 2026-07-06 — Codex

**무엇을 했나:** 합의된 정책을 기준으로 `.harness/PLAN.md`에 `AIverse MVP 구현 로드맵` 초안을 작성했다. 백엔드 공통 기반부터 인증, 콘텐츠, 파일, 크레딧, 구매, 대시보드, 프론트엔드 REST 전환, 전체 검증까지 9단계로 나누고 각 단계를 독립 검증 가능한 feature 체크리스트로 세분화했다.

**막힌 부분:** 없음. 구현 전 사용자 컨펌 대기.

**다음에 할 일:** 사용자가 로드맵을 컨펌하면 `(제안)`을 `(확정)`으로 바꾸고 1단계 `백엔드 공통 기반`부터 구현한다.

**참고사항:** 이번 작업은 계획 문서만 변경했으며 실제 애플리케이션 코드는 수정하지 않았다.

---

## 2026-07-06 — Codex

**무엇을 했나:** `.harness/ARCHITECTURE.md`와 `.harness/PLAN.md`, `.harness/DECISIONS.md`를 갱신해 콘텐츠 검색/정렬/필터와 창작자 대시보드 기준을 확정.
- `GET /api/contents` 검색 대상: 제목·설명·태그
- `GET /api/contents` 필터: `type`, `categoryId`, `tag`, `minPrice`, `maxPrice`, `creatorId`
- `GET /api/contents` 정렬: `LATEST`·`POPULAR`·`PRICE_ASC`·`PRICE_DESC`
- 정렬 구현: `Querydsl` 동적 쿼리
- `GET /api/dashboard/sales`: `period`(`7D`·`30D`·`ALL`) + `totals`·`series`·`items`

**막힌 부분:** 없음.

**다음에 할 일:** `PLAN.md`의 남은 항목인 "합의 내용을 기반으로 백엔드 및 프론트엔드 연동 단계를 작은 feature로 분리"를 backend/frontend 구현 단위로 쪼갠다. 그 다음 실제 구현을 시작한다.

**참고사항:** 정렬은 고정 쿼리 대신 `Querydsl`로 조합하고, 나머지 탐색/대시보드 기준은 앞서 합의한 추천안을 그대로 반영했다.

---

## 2026-07-06 — Claude Code

**무엇을 했나:** 프론트엔드 코드 전체(페이지 10개, admin 3개, SDK/스토어/훅)를 분석해 `.harness/ARCHITECTURE.md`의 API 명세가 실제 프론트 요구사항을 빠짐없이 커버하는지 검증. 발견한 갭을 사용자와 합의해 명세에 반영:
- `asset.view_count` 컬럼 추가 (좋아요 `like_count`는 UI에 누르는 상호작용이 없어 보류)
- `GET /api/contents/{id}` 호출마다 조회수 무조건 +1
- `GET /api/contents` 쿼리 파라미터에 `creatorId` 등 명시
- `GET /api/dashboard/sales` 응답 shape을 `{totals, items}`로 확정
- 백엔드 변경이 필요 없는 프론트 기술부채(좋아요 필드 미구현, 이중 인증 로직, Upload.jsx의 클라이언트발 신뢰 불가 필드 등)는 `BACKLOG.md`로 분리

**막힌 부분:** 없음.

**다음에 할 일:** `PLAN.md`의 "AIverse 개발 계획 구체화"에 남은 항목(카테고리 데이터 모델, 창작자 수익·수수료 정책, 파일 유형/용량 제한)을 마저 확정한 뒤, `aiverse-backend-builder`로 Content 모듈부터 실제 구현 시작.

**참고사항:** 이번 분석은 다른 세션이 `PLAN.md`/`DECISIONS.md`에 인증·저장소·트랜잭션 정책을 상당히 구체화해둔 상태에서 이어받아 진행함 — 크로스 툴 하네스가 의도대로 작동 중.

---

## 2026-07-05 — Claude Code

**무엇을 했나:**
1. 크로스 툴(Claude Code/Codex/Cursor) 작업 연속성 하네스(`.harness/`, `CLAUDE.md`, `AGENTS.md`, `.cursor/rules/harness.mdc`)를 구성.
2. 루트/frontend/backend에 흩어져 있던 SPEC.md 3개를 `.harness/ARCHITECTURE.md`로 통합하고 원본 삭제, README 링크 갱신.
3. AIverse 백엔드 구현을 위한 Claude Code 전용 에이전트 팀(`backend-architect` → `api-builder` → `qa` 파이프라인 + 점진적 QA)과 오케스트레이터(`aiverse-backend-builder`)를 생성.

**막힌 부분:** 없음.

**다음에 할 일:** "AIverse 백엔드 구현해줘"라고 요청하면 `aiverse-backend-builder` 오케스트레이터가 Content → Credit → Purchase/Library → Dashboard 순으로 실제 Spring Boot 코드(Entity/Repository/Service/Controller)를 생성한다. 아직 실제 구현은 시작되지 않았다 — 팀/스킬 스캐폴딩만 완료된 상태.

**참고사항:**
- `.harness/ARCHITECTURE.md`에 DB 스키마·API 명세가 전부 있으니 코드베이스를 처음부터 다시 탐색할 필요 없음.
- 백엔드 에이전트 팀(`aiverse-backend-builder`)은 Claude Code 전용(`TeamCreate`/`SendMessage` 사용)이라 Codex/Cursor에서는 트리거되지 않는다. Codex/Cursor 세션에서 백엔드 작업을 이어갈 때는 이 오케스트레이터 없이 `.harness/ARCHITECTURE.md`를 참고해 직접 구현해야 한다.
