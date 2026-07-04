# Frontend 명세

## Tech Stack

| 항목 | 버전 / 도구 |
| --- | --- |
| Runtime | Node.js >= 20.19.0 |
| Package Manager | Yarn |
| Framework | React 18 |
| Build Tool | Vite 8 |
| Styling | TailwindCSS 3, shadcn/ui |
| Routing | React Router 7 |
| State | Zustand, TanStack Query |
| Form | React Hook Form, Zod |

## 폴더 구조

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
│   ├── hooks/               # 커스텀 훅
│   ├── lib/                 # 인증, 유틸, 프로바이더
│   ├── pages/               # 페이지 컴포넌트
│   ├── sdk/                 # VibeX SDK 소스 (전환 예정)
│   ├── stores/              # Zustand 스토어
│   ├── App.jsx              # 라우터 및 앱 진입점
│   ├── Layout.jsx           # 공통 레이아웃 (Navbar, Footer)
│   ├── main.jsx             # React DOM 마운트
│   └── pages.config.js      # 페이지·레이아웃 설정
├── vite-plugins/            # Vite 개발용 플러그인 (production 빌드 제외)
├── index.html
├── vite.config.js
├── tailwind.config.js
├── package.json
└── components.json          # shadcn/ui 설정
```

## 라우팅

`pages.config.js`에서 페이지를 등록하고, `App.jsx`에서 React Router로 매핑합니다.

| 경로 | 컴포넌트 | 파일 |
| --- | --- | --- |
| `/` | Home | `pages/Home.jsx` |
| `/Explore` | Explore | `pages/Explore.jsx` |
| `/content/:slug` | ContentDetail | `pages/ContentDetail.jsx` |
| `/Credits` | Credits | `pages/Credits.jsx` |
| `/Upload` | Upload | `pages/Upload.jsx` |
| `/Library` | Library | `pages/Library.jsx` |
| `/Dashboard` | Dashboard | `pages/Dashboard.jsx` |
| `/Profile` | Profile | `pages/Profile.jsx` |
| `/Login` | Login | `pages/Login.jsx` |
| `/Register` | Register | `pages/Register.jsx` |

## API 연동

### 현재 (VibeX SDK)

| 모듈 | 용도 |
| --- | --- |
| `entities.js` | Content, Category, User, Purchase, CreditTransaction CRUD |
| `vibexClient.js` | VibeX API 클라이언트 (`app.vibe-x.app`) |
| `integrations.js` | 파일 업로드, 프로젝트 정보 조회 |
| `sdk/index.js` | VibeX SDK 구현체 |

인증 토큰은 `localStorage.access_token`에 저장합니다.

### 전환 예정 (Spring Boot REST API)

| VibeX SDK | Spring API (예정) |
| --- | --- |
| `Auth.login` | `POST /api/auth/login` |
| `Auth.register` | `POST /api/auth/register` |
| `vibex.auth.me()` | `GET /api/auth/me` |
| `Content.paging` | `GET /api/contents` |
| `Category.paging` | `GET /api/categories` |
| `UploadFile` | `POST /api/files/upload` |

환경 변수 `VITE_API_URL`로 API 서버 주소를 설정합니다.

## 환경 변수

| 변수 | 설명 | 예시 |
| --- | --- | --- |
| `VITE_API_URL` | Spring API Base URL (전환 후) | `http://localhost:8080/api` |
| `VITE_VIBEX_APP_ID` | VibeX App ID (현재) | — |
| `VITE_VIBEX_BACKEND_URL` | VibeX API URL (현재) | — |
| `VITE_APP_ENV` | `production` 시 VibeX dev 플러그인 비활성화 | `production` |

## Scripts

| Command | Description |
| --- | --- |
| `yarn dev` | 개발 서버 (port 5173) |
| `yarn dev:low-mem` | 저메모리 모드 개발 서버 (256MB) |
| `yarn build` | 프로덕션 빌드 → `dist/` |
| `yarn preview` | 프로덕션 빌드 미리보기 |

## 주요 의존성

| 패키지 | 용도 |
| --- | --- |
| `@tanstack/react-query` | 서버 상태 캐싱 |
| `zustand` | 클라이언트 전역 상태 (`useAppStore`) |
| `react-router-dom` | SPA 라우팅 |
| `framer-motion` | 애니메이션 |
| `lucide-react` | 아이콘 |
| `recharts` | 차트 (대시보드) |
