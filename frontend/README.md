# Frontend

Aiverse 사용자-facing 웹 애플리케이션입니다. 창작자와 구매자가 AI 디지털 콘텐츠를 등록, 탐색, 구매, 관리할 수 있는 UI를 제공합니다.

## 역할

프론트엔드는 서비스의 모든 사용자 경험을 담당합니다. 백엔드 API를 호출해 데이터를 표시하고, 사용자 입력을 API 요청으로 전달합니다.

- **구매자** — 콘텐츠 탐색, 상세 확인, 크레딧 충전, 구매, 보관함
- **창작자** — 콘텐츠 업로드, 판매 대시보드, 프로필 관리
- **공통** — 회원가입, 로그인, 인증 상태 관리

## 주요 화면

| 경로 | 화면 | 설명 |
| --- | --- | --- |
| `/` | Home | 서비스 소개, 인기 콘텐츠 |
| `/Explore` | Explore | 콘텐츠 탐색 (유형·카테고리·정렬 필터) |
| `/content/:slug` | ContentDetail | 콘텐츠 상세 및 구매 |
| `/Credits` | Credits | 크레딧 충전 |
| `/Upload` | Upload | AI 창작물 등록 |
| `/Library` | Library | 구매 보관함 |
| `/Dashboard` | Dashboard | 창작자 판매 대시보드 |
| `/Profile` | Profile | 프로필 수정 |
| `/Login`, `/Register` | Login, Register | 인증 |

## 기술 개요

React 18 + Vite + TailwindCSS 기반 SPA입니다. React Router로 페이지를 구성하고, Zustand로 클라이언트 상태를, TanStack Query로 서버 상태를 관리합니다.

현재 API 연동은 VibeX SDK를 사용 중이며, Spring Boot 백엔드 REST API로 전환할 예정입니다.

## Getting Started

```bash
yarn install
yarn dev
```

앱은 **http://localhost:5173** 에서 확인할 수 있습니다.

## 배포

Vercel 배포를 기준으로 합니다. 빌드 결과물은 `dist/` 디렉터리에 생성됩니다.

```bash
yarn build
yarn preview   # 로컬에서 production 빌드 확인
```

## 문서

- [SPEC.md](./SPEC.md) — 폴더 구조, 라우팅, API 연동, 환경 변수 등 상세 명세
