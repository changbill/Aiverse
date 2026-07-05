# BACKLOG — 미해결 이슈 및 향후 작업

> `PLAN.md`가 "지금 진행 중인 계획"이라면, 이 문서는 "지금 당장은 아니지만 언젠가 처리해야 할 것"을 담는다.
> 버그, 기술 부채, 나중에 고려할 기능 아이디어를 우선순위 없이 나열하고, 실제로 착수할 때 `PLAN.md`로 승격한다.

## 알려진 이슈

_(없음)_

## 기술 부채

- Frontend가 아직 VibeX SDK(`src/api/vibexClient.js`)를 사용 중 — Spring Boot REST API로 교체 필요 (`ARCHITECTURE.md` 참조)
- Backend에 Entity/Repository/Controller 미구현 — 도메인 모델부터 설계 필요
- 프론트가 `ContentCard`/`ContentDetail`에 `likes`(좋아요 수) 필드를 표시하지만 백엔드에 대응 컬럼/API가 없고 "누르는" 상호작용도 없음 — 필드 표시를 제거하거나, 실제 찜하기 기능으로 만들지 결정 필요 (`frontend/src/components/ContentCard.jsx`, `frontend/src/pages/ContentDetail.jsx`)
- `AuthProvider`(vibex `auth.me()` 기반)와 각 페이지의 Zustand `useAppStore().user` 가드 로직이 이중으로 존재 — 어느 쪽이 진짜 인증 소스인지 정리 필요 (Spring 전환 시 함께 리팩터링)
- Register.jsx가 회원가입 성공 후 별도로 로그인 API를 한 번 더 호출 (자동 로그인 목적) — 회원가입 응답에서 바로 토큰을 주는 방식으로 단순화할지 검토
- Upload.jsx가 `slug`/`sales`/`views`/`likes`/`status`/`createdAt` 등을 클라이언트에서 채워 서버로 전송 — Spring API는 이 값들을 신뢰하지 말고 서버에서 재계산/검증해야 함 (특히 slug 유일성)
- 크레딧 패키지(Credits.jsx)와 판매자 스포트라이트(Home.jsx)가 프론트에 하드코딩됨 — `GET /api/credit-products` 연동 시 하드코딩 제거 필요

## 향후 아이디어

- 좋아요/찜, 리뷰·평점, 알림, 창작자 팔로우, 신고, 검색 자동완성 — 현재 프론트 어디에도 상호작용이 없어 신규 기능으로 별도 요청 시 설계
- Google 소셜 로그인 — VibeX SDK에 전체 구현되어 있으나 로그인 화면에 버튼 없음 (사용 여부 확인 필요)
