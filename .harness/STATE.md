# STATE — 진행 상황 스냅샷

> 이 문서는 세션 로그가 **아니라** "지금까지 무엇이 끝났는가"의 단계 단위 요약이다.
> 세션별 서술(무엇을 했고 무엇이 막혔는지)은 `HANDOFF.md`에, 결정과 이유는 `DECISIONS.md`에 남긴다 — 여기에 다시 옮기지 않는다.
> 단계가 끝나면 그 단계를 한 줄로 갱신한다. 이슈/커밋 단위로 로그를 쌓지 않는다.

**마지막 갱신:** 2026-07-06

## 완료된 단계

- **하네스 구성**: 크로스 툴(Claude Code/Codex/Cursor) 작업 연속성 하네스(`.harness/`, `CLAUDE.md`, `AGENTS.md`, `.cursor/rules/harness.mdc`) 구성. 프론트엔드/백엔드 모노레포 분리, 서비스 기획과 명세를 `ARCHITECTURE.md`로 통합.
- **1단계 백엔드 공통 기반**: Gradle 의존성(Security/JPA/Querydsl/Flyway/JWT/S3/Testcontainers), `local`/`test`/`prod` 프로필, Docker Compose(MySQL·MinIO) 로컬 환경, 공통 응답(`ApiResponse`/`PageResponse`)·오류 처리·`X-Request-Id` 전파, Flyway 초기 스키마(V1)·기준 데이터(V2), Testcontainers 통합 테스트 공통 설정(`IntegrationTestSupport`)까지 전체 완료.
- **2단계 회원과 인증**: `User`/`RefreshToken` Entity+Repository(3계층), 회원가입·로그인·현재 사용자 조회, Swagger UI(springdoc), Access token 발급과 `SecurityFilterChain`, Refresh token 쿠키·해시 저장·회전·로그아웃, 입력 검증·인증 오류 테스트까지 전체 완료.

## 확립된 컨벤션 (요약 — 근거·배경은 `DECISIONS.md` 참조)

- Repository는 도메인 인터페이스 + `jpa`(Spring Data JPA) + `impl`(어댑터) 3계층. Service는 도메인 인터페이스에만 의존.
- 모든 백엔드 구현(Repository/Service/Controller)은 TDD(실패하는 테스트 우선 → 최소 구현 → 리팩터링).
- Controller는 `ApiResponse<T>`/`PageResponse<T>`만 반환(`ResponseEntity` 금지, 비-200 상태는 `@ResponseStatus`), 라우팅만 담당하고 보조 로직은 `util`로 분리.
- 연관관계 N+1은 명시적 JPQL/Querydsl fetch join으로 해결(`@EntityGraph` 금지). 페이징 목록은 `XToOne`만 fetch join하고 목록·상세 DTO를 분리.
- Swagger UI(springdoc-openapi 3.0.3+) 제공 — Controller마다 `@Tag`/`@Operation`/(인증 필요 시) `@SecurityRequirement`.
- AIverse 백엔드 구현은 서브에이전트 팀 없이 단일 에이전트가 직접 수행 (`aiverse-backend-builder` 스킬 미사용).
- 소스 코드 변경 없는 순수 문서/설정 작업은 feature 브랜치 없이 master에 바로 커밋.
- 브랜치 전략: `PLAN.md` 단계 하나당 브랜치 하나, 체크리스트 항목 하나당 커밋 하나 (자세한 내용은 `CLAUDE.md`의 "하네스: 브랜치 전략" 참조).

## 진행 중인 단계

- **3단계 카테고리·태그·콘텐츠 탐색** (`feature/12-...`): `Category`/`Tag`/`Asset`/`AssetTag` Entity+Repository, `GET /api/categories`·`GET /api/tags`, 최소 `Purchase` 읽기 모델과 Querydsl 콘텐츠 검색·필터·정렬·페이지네이션 및 `GET /api/contents` 목록 완료. 나머지 2개 체크리스트 항목 진행 예정 — 자세한 내용은 `PLAN.md` 참조.

## 다음 단계

`PLAN.md` 3단계의 남은 항목 → 4단계 파일 업로드와 콘텐츠 관리 → ... 9단계 전체 검증과 문서화 순.
