## 하네스: 크로스 툴 작업 연속성

**목표:** Claude Code, Codex, Cursor 중 어떤 도구로 세션을 시작하든, 이전 작업 맥락(계획·진행상황·인수인계)을 이어서 파악하고 작업할 수 있게 한다.

**세션 시작 시 반드시 먼저 읽을 것 (이 순서로):**

1. `.harness/HANDOFF.md` — 직전 세션이 어디서 멈췄는지
2. `.harness/STATE.md` — 지금까지 무엇이 완료되었는지
3. `.harness/ARCHITECTURE.md` — 기술 스택/구조 요약 (코드베이스 재탐색 최소화)
4. `.harness/PLAN.md` — 제안·확정·진행 중인 계획
5. 필요 시 `.harness/DECISIONS.md`(과거 결정 이유), `.harness/BACKLOG.md`(미해결 항목)

**문서별 책임 (중복 기록 금지 — 아래 표에 없는 문서에는 해당 내용을 쓰지 않는다):**

| 문서 | 담는 내용 | 담지 않는 내용 |
| --- | --- | --- |
| `HANDOFF.md` | 세션마다 무엇을 했는지 (append-only 서술형 로그) | 단계별 완료 요약(STATE 몫), 결정 이유(DECISIONS 몫) |
| `STATE.md` | 지금까지 끝난 것의 단계 단위 요약 스냅샷 | 세션별 서술(HANDOFF 몫). 이슈 하나하나를 로그처럼 쌓지 않는다 — 단계가 끝나면 그 단계 한 줄로 갱신 |
| `ARCHITECTURE.md` | 지금의 기술 스택/폴더 구조/컨벤션 (현재 상태) | 왜 그렇게 정했는지(DECISIONS 몫), 진행 상황(STATE 몫) |
| `DECISIONS.md` | 결정 내용과 이유의 역사(append-only) | 구현 여부/진행 상황(STATE 몫) |
| `PLAN.md` | 아직 안 끝난 계획과 체크리스트만 | 완료된 항목(체크만 남기지 말고 STATE로 옮긴 뒤 제거) |
| `BACKLOG.md` | 지금 하지 않지만 나중에 할 것(버그·기술부채·아이디어) | 진행 중인 계획(PLAN 몫) |

**작업 워크플로우 (필수):**

- 새로운 기능/변경 요청을 받으면, 바로 구현하지 말고 `.harness/PLAN.md`에 계획 초안을 작성한다.
- 사용자에게 계획을 제시하고 피드백을 받아 반영하는 과정을 반복한다.
- 사용자가 명시적으로 컨펌하면 계획을 확정 상태로 바꾸고 구현을 시작한다.
- `PLAN.md` 단계별 체크리스트 항목은 하나씩 구현이 끝날 때마다 즉시 `.harness/STATE.md`에 반영하고, 그 항목을 `PLAN.md`에서 제거한다. `STATE.md`에는 위 표대로 단계 단위 한 줄 요약만 남기고 세션 서술은 남기지 않는다.
- 구현 완료 후 `.harness/STATE.md`를 갱신한다.
- 세션을 종료하거나 작업을 중단할 때 `.harness/HANDOFF.md`에 다음 세션을 위한 인수인계를 남긴다.
- 아키텍처/워크플로우에 대한 중요한 결정을 내리면 `.harness/DECISIONS.md` 표의 최상단에 이유와 함께 기록해 최신 결정이 위에 오도록 유지한다.

**트리거:** 이 프로젝트에서의 모든 작업 요청에 위 워크플로우를 적용하라. 단순 질문(코드 설명 등)은 하네스 절차 없이 바로 응답 가능.

## 하네스: 테스트 실행 정책

**목표:** Claude Code, Codex, Cursor 중 어떤 도구로 작업하든 검증 시 동일한 테스트 범위를 적용한다.

- 구현·수정 후 **기본 검증**은 **단위 테스트만** 실행한다 (`cd backend && ./gradlew test` — 통합 테스트 제외).
- **TDD로 통합 테스트를 작성·수정하는 작업** 중에는 해당 통합 테스트를 반드시 실행한다 — red → green → refactor 사이클을 지킨다. 해당 테스트 클래스만 실행해도 된다 (예: `./gradlew integrationTest --tests com.example.aiverse.repository.AssetRepositoryTest`).
- **통합 테스트 전체 스위트**(`cd backend && ./gradlew integrationTest`)는 사용자가 명시적으로 요청한 경우에만 실행한다.
- 단위 테스트: Service(Mockito), Controller(MockMvc·Mockito) 등 통합 테스트 베이스를 상속하지 않는 테스트.
- Repository 통합 테스트: `RepositoryIntegrationTestSupport` (`@DataJpaTest` 슬라이스 + Testcontainers MySQL).
- 전체 컨텍스트 통합 테스트: `IntegrationTestSupport` (`@SpringBootTest`) — `AiverseApplicationTests`, `SecurityFilterChainTest` 등.

## 하네스: 통합 테스트 구조

**목표:** MySQL 실제 동작 검증은 유지하면서 통합 테스트 기동 비용을 줄인다.

| 베이스 클래스 | 어노테이션 | 용도 |
| --- | --- | --- |
| `RepositoryIntegrationTestSupport` | `@DataJpaTest` + Testcontainers | `*RepositoryTest` — JPA·Querydsl·Flyway만 기동 (Security·Web·OpenAPI 제외) |
| `IntegrationTestSupport` | `@SpringBootTest` + Testcontainers | 앱 기동 검증, Security 필터 체인, MockMvc 전체 스택 |

- Testcontainers MySQL은 `TestcontainersConfiguration`의 Spring `@Bean`으로 관리하고 `withReuse(true)` + `src/test/resources/testcontainers.properties`(`testcontainers.reuse.enable=true`)로 컨테이너를 재사용한다.
- Repository 통합 테스트 작성 시 `RepositoryIntegrationTestSupport`를 상속한다. 새 `*RepositoryImpl`·`*QuerydslRepository` 추가 시 `RepositoryIntegrationTestConfiguration`에 등록한다.
- 테스트별 데이터 격리는 `@Transactional` 롤백을 유지한다.

## 하네스: JPA 조회 최적화

- 연관관계 조회에서 N+1 문제가 발생하면 명시적 JPQL/Querydsl fetch join으로 해결한다. `@EntityGraph`는 사용하지 않는다.
- 페이징 조회에서는 `XToMany` 컬렉션 fetch join을 사용하지 않는다. DB 페이징이 아닌 메모리 페이징과 중복 행 문제가 발생할 수 있다.
- 페이징 목록 쿼리는 `ManyToOne`·`OneToOne` 등 `XToOne` 연관관계만 fetch join한다.
- API 응답 DTO는 목록용과 상세용으로 분리한다. 목록 DTO는 페이징 쿼리가 `XToOne` 연관관계만으로 완성되도록 설계하고, `XToMany` 컬렉션이 필요한 응답은 상세 조회 DTO에서 제공한다.
- 목록에서 컬렉션 정보가 반드시 필요하면 컬렉션 fetch join 대신 별도 일괄 조회, DTO projection, 집계 쿼리 등 페이징을 보존하는 방식을 사용한다.

## 하네스: 브랜치 전략

**목표:** Claude Code, Codex, Cursor 중 어떤 도구로 구현을 시작하든 동일한 브랜치 규칙을 따라 작업 이력을 일치시킨다.

- 작업 브랜치는 항상 `feature/...` 형식을 사용한다.
- 현재 브랜치가 `feature/...` 형식이 아니면, 구현 시작 전에 새 브랜치를 생성하고 전환한다.
- 소스 코드 변경이 없는 순수 문서/설정 작업(`.harness/*.md` 갱신, 컨벤션·결정 기록, 에이전트 정의 파일 등)은 feature 브랜치 없이 master에 바로 커밋한다. 실제 코드 변경이 있는 작업만 이 브랜치 전략을 따른다.
- **브랜치 하나 = `PLAN.md`의 단계(예: "3단계 카테고리·태그·콘텐츠 탐색") 하나.** 그 단계의 체크리스트 항목(todo) 하나당 커밋 하나로 진행하며, 각 커밋에는 구현과 관련 하네스 문서 갱신(`PLAN.md` 체크 제거, `STATE.md` 반영 등)을 함께 담는다.
- 브랜치 이름 형식: `feature/{번호}-{단계 이름 키워드}`
  - `{번호}`는 이전에 사용한 feature 번호 다음 순번이다. 외부 이슈 트래커와 연동하지 않으며, `git log`에서 가장 최근 `merge: ...` 커밋의 번호를 확인해 다음 번호를 정한다.
  - `{단계 이름 키워드}`는 단계 제목에서 핵심 키워드 2~4개를 뽑아 한글 그대로 하이픈(-)으로 연결해 요약한다 (공백·특수문자 제거, 영문 로마자 표기로 바꾸지 않는다). 마지막은 동사로 끝낸다.
  - 예: 다음 번호 12, 단계 "카테고리·태그·콘텐츠 탐색" → `feature/12-카테고리-태그-콘텐츠탐색-구현`
- 한 단계의 체크리스트를 모두 구현·커밋한 뒤 master에 병합하고, 사용자에게 완료를 보고해 확인을 받은 뒤에만 다음 단계의 브랜치를 새로 만든다.
- master에 병합한 뒤에는 병합된 feature 브랜치를 삭제하고 master 브랜치로 전환한 상태를 유지한다. 다음 단계는 항상 master에서 새 브랜치를 생성해 시작한다.
- 브랜치·커밋·병합은 로컬에서만 진행한다. 원격 push나 PR 생성은 사용자가 별도로 요청할 때만 수행한다.

## 하네스: AIverse 백엔드 구현 — 단일 에이전트

**목표:** `.harness/ARCHITECTURE.md`에 명세된 DB 스키마와 API를 Spring Boot 코드로 구현.

이 프로젝트는 단일 에이전트로 충분히 처리 가능한 규모다. Entity 설계, Repository/Service/Controller 구현, 검증까지 모두 Read/Edit/Write/Bash로 직접 수행하고, `backend-architect`/`api-builder`/`qa` 서브에이전트나 `aiverse-backend-builder` 스킬의 팀 기반 워크플로우(TeamCreate/SendMessage)는 사용하지 않는다. 서브에이전트 호출은 매번 여러 분이 걸려 이 규모의 작업에는 비효율적이다. 정말 막혀서 다른 관점이 필요한 경우가 아니면 에이전트를 새로 띄우지 말고 직접 구현한다.
