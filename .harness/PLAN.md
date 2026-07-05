# PLAN — 현재 구현 계획

> 새 기능/변경을 시작하기 전에 이 문서에 계획을 먼저 적고, 사용자 피드백을 받아 확정한 뒤 구현을 시작한다.
> 계획이 확정되지 않은 항목은 `[ ] (제안)`으로 표시하고, 사용자 컨펌 후 `(제안)`을 지운다.
> 구현이 끝난 항목은 체크하고 `STATE.md`에 완료 사실을 옮겨 기록한다.

## 진행 중인 계획

### AIverse 백엔드 구현 에이전트 팀 — (확정, 팀 구성 완료 / 모듈 구현 대기)

**배경:** backend는 `AiverseApplication.java`만 있는 초기 상태. Entity/API 구현이 최우선 순위로 확정됨.
**범위:** Spring Boot + MySQL 기반 Content/Credit/Purchase/License/User 도메인 모델링과 REST API 구현. Frontend 실제 연동(VibeX→Spring 교체)은 별도 작업으로 분리.
**팀 구성:** `backend-architect`(도메인 모델링) → `api-builder`(Repository/Service/Controller 구현) → `qa`(모듈별 점진적 검증), 에이전트 팀 모드
**단계:**
- [x] `.claude/agents/backend-architect.md`, `.claude/agents/api-builder.md`, `.claude/agents/qa.md` 생성
- [x] `.claude/skills/aiverse-backend-builder/SKILL.md` 오케스트레이터 생성
- [ ] Content 모듈 (architect→builder→QA) — "AIverse 백엔드 구현해줘"로 트리거하여 실행
- [ ] Credit 모듈
- [ ] Purchase/Library 모듈
- [ ] Creator Dashboard 모듈
**영향받는 영역:** backend
**참고:** 실제 코드 구현은 아직 시작하지 않음. 팀/오케스트레이터 스캐폴딩만 완료된 상태.

## 계획 작성 형식

```markdown
### {기능/작업명} — (제안 | 확정 | 진행중 | 완료)

**배경:** 왜 필요한가
**범위:** 무엇을 하고, 무엇을 하지 않는가
**단계:**
- [ ] 단계 1
- [ ] 단계 2
**영향받는 영역:** frontend / backend / 둘 다
```
