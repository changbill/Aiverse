---
name: aiverse-backend-builder
description: "AIverse 백엔드(Spring Boot) 구현 에이전트 팀을 조율하는 오케스트레이터. 'AIverse 백엔드 구현해줘', '백엔드 API 만들어줘', 'Entity 설계해줘', '컨텐츠/크레딧/구매/대시보드 API 구현' 요청 시 사용. 후속 작업: 백엔드 일부 모듈만 다시 구현, 백엔드 API 수정/보완, 이전 백엔드 결과 개선, 새 엔드포인트 추가 요청 시에도 반드시 이 스킬을 사용."
---

# AIverse Backend Builder — Orchestrator

AIverse 백엔드(Spring Boot + JPA + MySQL) 구현을 위한 에이전트 팀을 조율하여, `.harness/ARCHITECTURE.md`에 명세된 DB 스키마와 API를 실제 코드로 구현하는 통합 스킬.

## 실행 모드: 에이전트 팀

## 브랜치 전략

- 작업 브랜치는 항상 `feature/...` 형식을 사용한다.
- 현재 브랜치가 `feature/...` 형식이 아니면, 구현 시작 전에 새 브랜치를 생성하고 전환한다.
- 브랜치 이름 형식: `feature/{이슈번호}-{이슈제목}`
  - `{이슈번호}`와 `{이슈제목}`은 [Aiverse 이슈 트래커 노션 DB](https://app.notion.com/p/51e1299b04014c34a4fab854f4248be0?v=7e2fed7703af4ca79fad7afe3765d3c2)의 "이슈 번호"·"이슈" 속성을 조회해 정한다.
  - `{이슈제목}`은 해당 이슈 제목에서 핵심 키워드 2~4개를 뽑아 한글 그대로 하이픈(-)으로 연결해 요약한다 (공백·특수문자 제거, 영문 로마자 표기로 바꾸지 않는다). `유저-리프레시토큰-엔티티-구현`처럼 마지막은 동사로 끝낸다.
  - 예: 이슈 번호 7, 이슈 "User·RefreshToken 엔티티와 Repository 구현" → `feature/7-유저-리프레시토큰-엔티티-구현`
  - 여러 이슈를 한 번에 진행하는 모듈 단위 작업이면, 그 모듈의 가장 앞선 이슈 번호를 대표로 사용한다.

## 에이전트 구성

| 팀원 | 에이전트 타입 | 역할 | 정의 파일 | 출력 |
| --- | --- | --- | --- | --- |
| architect | 커스텀 (`backend-architect`) | Entity/DB 연동 설계 | `.claude/agents/backend-architect.md` | `backend/src/main/java/.../entity/*.java` |
| builder | 커스텀 (`api-builder`) | Repository/Service/Controller/DTO 구현 | `.claude/agents/api-builder.md` | `backend/src/main/java/.../{repository,service,controller,dto}/*.java` |
| qa | 커스텀 (`qa`, `general-purpose` 기반) | 모듈별 점진적 정합성 검증 | `.claude/agents/qa.md` | `_workspace/qa_{module}_report.md` |

## 워크플로우

### Phase 0: 컨텍스트 확인 (후속 작업 지원)

1. `backend/src/main/java/com/example/aiverse/entity/` 존재 여부와 `_workspace/` 존재 여부를 확인한다.
2. 실행 모드 결정:
   - **Entity 디렉토리 없음** → 초기 실행. Phase 1로 진행.
   - **Entity 일부 존재 + 사용자가 특정 모듈 수정/추가 요청** → 부분 재실행. 해당 모듈만 팀에 재할당.
   - **Entity 존재 + 사용자가 새 요구사항 제공** → 기존 `_workspace/`를 `_workspace_{YYYYMMDD_HHMMSS}/`로 이동 후 Phase 1 진행.
3. 부분 재실행 시, 기존 Entity/API 파일 경로를 팀원 프롬프트에 포함하여 처음부터 다시 만들지 않고 이어서 작업하도록 지시한다.

### Phase 1: 준비

0. 현재 git 브랜치가 `feature/...` 형식인지 확인한다. 아니라면 '브랜치 전략'에 따라 이슈번호·이슈제목을 정하고 새 브랜치를 생성·전환한 뒤 다음 단계로 진행한다.
1. `.harness/ARCHITECTURE.md`의 "DB 스키마"·"API 명세" 섹션을 읽고, 아래 4개 모듈로 작업을 분해한다:
   - **Content**: `asset`, `tag`, `asset_tag` + `/api/contents`, `/api/categories`, `/api/tags`
   - **Credit**: `credit_product`, `payment`, `credit_transaction` + `/api/credit-products`, `/api/payments`, `/api/credit-transactions`
   - **Purchase/Library**: `purchase`, `download` + `/api/purchases`, `/api/library`, `/api/downloads`
   - **Dashboard**: `creator_settlement` + `/api/dashboard/sales` (+ `user`/`auth`는 Content 모듈과 함께 최초 처리)
2. `_workspace/` 생성 (초기 실행 시, 또는 새 실행에서 기존 폴더를 보관 디렉토리로 이동한 직후).
3. 사용자에게 모듈 분해와 순서를 간단히 보고한다 (계획 확정은 이미 완료된 상태이므로 승인 재요청은 생략 가능, 단 사용자가 순서 변경을 원하면 반영).

### Phase 2: 팀 구성

```
TeamCreate(
  team_name: "aiverse-backend-team",
  members: [
    { name: "architect", agent_type: "backend-architect", model: "opus", prompt: "AIverse 백엔드 도메인 모델링 담당. .harness/ARCHITECTURE.md의 DB 스키마를 JPA Entity로 구현." },
    { name: "builder", agent_type: "api-builder", model: "opus", prompt: "AIverse 백엔드 API 구현 담당. architect의 Entity를 기반으로 Repository/Service/Controller/DTO 구현." },
    { name: "qa", agent_type: "qa", model: "opus", prompt: "AIverse 백엔드 QA 담당. 모듈 완성 직후 Entity-스키마, API-명세 정합성을 점진적으로 검증." }
  ]
)
```

```
TaskCreate(tasks: [
  { title: "Content Entity 설계", assignee: "architect" },
  { title: "Content API 구현", assignee: "builder", depends_on: ["Content Entity 설계"] },
  { title: "Content QA", assignee: "qa", depends_on: ["Content API 구현"] },
  { title: "Credit Entity 설계", assignee: "architect" },
  { title: "Credit API 구현", assignee: "builder", depends_on: ["Credit Entity 설계"] },
  { title: "Credit QA", assignee: "qa", depends_on: ["Credit API 구현"] },
  { title: "Purchase/Library Entity 설계", assignee: "architect", depends_on: ["Content Entity 설계", "Credit Entity 설계"] },
  { title: "Purchase/Library API 구현", assignee: "builder", depends_on: ["Purchase/Library Entity 설계"] },
  { title: "Purchase/Library QA", assignee: "qa", depends_on: ["Purchase/Library API 구현"] },
  { title: "Dashboard Entity 설계", assignee: "architect", depends_on: ["Purchase/Library Entity 설계"] },
  { title: "Dashboard API 구현", assignee: "builder", depends_on: ["Dashboard Entity 설계"] },
  { title: "Dashboard QA", assignee: "qa", depends_on: ["Dashboard API 구현"] }
])
```

### Phase 3: 모듈별 파이프라인 실행

**실행 방식:** 팀원들이 공유 작업 목록에서 자체 조율. 리더는 진행 상황을 모니터링.

**흐름 (모듈당 반복):** `architect`가 Entity 완성 → `builder`에게 SendMessage로 알림 → `builder`가 API 구현 → `qa`에게 SendMessage로 알림 → `qa`가 즉시 점진적 검증 → 문제 발견 시 SendMessage로 담당자에게 피드백 → 수정 → 다음 모듈로 진행.

**산출물 저장:**

| 팀원 | 출력 경로 |
| --- | --- |
| architect | `backend/src/main/java/com/example/aiverse/entity/*.java` (실제 소스 경로 — `_workspace/`가 아님) |
| builder | `backend/src/main/java/com/example/aiverse/{repository,service,controller,dto,config,exception}/*.java` |
| qa | `_workspace/qa_{module}_report.md` |

> 본 하네스는 실제 프로덕션 코드(`backend/src/main/java/...`)를 직접 생성한다. `_workspace/`는 QA 리포트 등 중간 산출물 전용으로 사용한다.

**리더 모니터링:**
- 팀원이 유휴 상태가 되면 자동 알림 수신.
- 특정 모듈에서 막히면 SendMessage로 상태 확인 후 재지시.
- 전체 진행률은 TaskGet으로 확인.

### Phase 4: 통합 및 보고

1. 모든 모듈 완료 대기 (TaskGet으로 확인).
2. `qa`의 모든 모듈 리포트를 Read로 수집.
3. 차단급 이슈가 남아있지 않은지 최종 확인 (트랜잭션 경계, 크레딧 로직 등).
4. 향후 프론트 연동 정합성 관련 QA 발견사항은 `.harness/BACKLOG.md`에 추가한다.
5. `.harness/STATE.md`를 갱신한다 (완료된 모듈, 남은 작업).
6. `.harness/HANDOFF.md`에 이번 세션 요약을 기록한다.

### Phase 5: 정리

1. 팀원들에게 종료 요청 (SendMessage).
2. 팀 정리 (`TeamDelete`).
3. `_workspace/` 보존 (QA 리포트는 감사 추적용으로 유지).
4. 사용자에게 결과 요약 보고 (모듈별 구현 여부, QA 통과 여부, 남은 BACKLOG).

## 데이터 흐름

```
[architect] --SendMessage(Entity 완료)--> [builder] --SendMessage(API 완료)--> [qa]
     │                                          │                                  │
     ▼                                          ▼                                  ▼
 entity/*.java                    {repository,service,controller}/*.java   _workspace/qa_*.md
                                                                                    │
                                                                          문제 발견 시 SendMessage로
                                                                          architect/builder에게 피드백
```

## 에러 핸들링

| 상황 | 전략 |
| --- | --- |
| architect가 스키마 해석 모호성 보고 | 리더가 사용자에게 확인 후 지시, 팀 재개 |
| builder가 빌드 실패(컴파일 에러) | builder가 1회 자체 수정 시도, 재실패 시 리더에게 보고 |
| qa가 차단급 이슈 발견 | 담당 에이전트에게 즉시 SendMessage, 수정 후 재검증 |
| 팀원 1명 응답 없음/중지 | 리더가 유휴 알림 수신 → SendMessage로 상태 확인 → 재시작 |
| 팀원 간 설계 의견 충돌 (예: 조인 테이블 처리 방식) | 리더가 개입해 사용자에게 확인 또는 팀원 합의 유도 |

## 테스트 시나리오

### 정상 흐름
1. 사용자가 "AIverse 백엔드 구현해줘" 요청.
2. Phase 0에서 Entity 디렉토리 없음 확인 → 초기 실행.
3. Phase 1에서 4개 모듈(Content/Credit/Purchase-Library/Dashboard)로 분해.
4. Phase 2에서 3인 팀 구성 + 12개 작업 등록.
5. Phase 3에서 Content부터 architect→builder→qa 순회, 문제없이 다음 모듈로 진행.
6. Phase 4에서 모든 모듈 완료 확인, STATE/HANDOFF 갱신.
7. Phase 5에서 팀 정리, 결과 보고.
8. 예상 결과: `backend/src/main/java/com/example/aiverse/{entity,repository,service,controller,dto}/` 하위에 4개 모듈 전체 구현 완료.

### 에러 흐름
1. Phase 3에서 `qa`가 Purchase 모듈의 크레딧 차감 로직에 `@Transactional` 누락을 발견 (차단급).
2. `qa`가 `builder`에게 SendMessage로 파일 경로와 문제, 수정 방향 전달.
3. `builder`가 수정 후 `qa`에게 재검증 요청.
4. `qa`가 재검증 통과 확인 후 리더에게 보고.
5. 재실패가 반복되면(2회 이상) 리더가 개입하여 직접 지시하거나 사용자에게 상황을 알린다.
