# BACKLOG — 미해결 이슈 및 향후 작업

> `PLAN.md`가 "지금 진행 중인 계획"이라면, 이 문서는 "지금 당장은 아니지만 언젠가 처리해야 할 것"을 담는다.
> 버그, 기술 부채, 나중에 고려할 기능 아이디어를 우선순위 없이 나열하고, 실제로 착수할 때 `PLAN.md`로 승격한다.
> **이 문서에 담지 않는 것:** 지금 진행 중인 계획(`PLAN.md` 몫).

## 알려진 이슈

_(없음)_

## 기술 부채

- 프론트가 `ContentCard`/`ContentDetail`에 `likes`(좋아요 수) 필드를 표시하지만 백엔드에 대응 컬럼/API가 없고 "누르는" 상호작용도 없음 — 필드 표시를 제거하거나, 실제 찜하기 기능으로 만들지 결정 필요 (`frontend/src/components/ContentCard.jsx`, `frontend/src/pages/ContentDetail.jsx`)
- Register.jsx가 회원가입 성공 후 별도로 로그인 API를 한 번 더 호출 (자동 로그인 목적) — 회원가입 응답에서 바로 토큰을 주는 방식으로 단순화할지 검토
- 판매자 스포트라이트(Home.jsx의 `creators` 목록)가 프론트에 하드코딩됨 — 실제 창작자 랭킹 API가 생기면 교체 필요
- VibeX 호스팅 iframe 미리보기/비주얼 에디터 연동 코드(`frontend/src/lib/VisualEditAgent.jsx`, `frontend/vite-plugins/*`, `vite.config.js`의 `iframe-hmr` 플러그인, `NavigationTracker.jsx`의 `window.parent.postMessage`)가 여전히 남아 있음 — 8단계에서는 API 통신에 관여하는 VibeX SDK/클라이언트(`src/sdk`, `src/api/{entities,vibexClient,integrations}.js`, `@vibexnpm/talkflow`)만 제거했고, 이 iframe 개발 도구들은 Vercel 단독 배포로 전환될 때 별도로 정리 필요

## 향후 아이디어

- 좋아요/찜, 리뷰·평점, 알림, 창작자 팔로우, 신고, 검색 자동완성 — 현재 프론트 어디에도 상호작용이 없어 신규 기능으로 별도 요청 시 설계
- Google 소셜 로그인 — 8단계에서 제거된 VibeX SDK에 구현되어 있었으나 백엔드 대응 API가 없어 이관하지 않음 (필요 시 신규 설계)
