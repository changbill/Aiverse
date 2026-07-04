# AIverse 명세

## 프로젝트 구조

```
aiverse/
├── README.md        # 서비스 기획 및 개요
├── SPEC.md          # 프로젝트 전체 명세 (현재 문서)
├── frontend/        # React 18 + Vite + TailwindCSS
│   ├── README.md    # 프론트엔드 개요
│   └── SPEC.md      # 프론트엔드 상세 명세
└── backend/         # Java 21 + Spring Boot + Gradle
    ├── README.md    # 백엔드 개요
    └── SPEC.md      # 백엔드 상세 명세
```

## Prerequisites

| 영역 | 필요 도구 |
| --- | --- |
| Frontend | Node.js >= 20.19.0, Yarn |
| Backend | Java 21, Gradle (Wrapper 포함), MySQL |

## 아키텍처

```
┌─────────────┐       REST API        ┌─────────────┐
│  Frontend   │  ──────────────────►  │   Backend   │
│  (Vercel)   │  ◄──────────────────  │  (Spring)   │
└─────────────┘       JSON            └──────┬──────┘
                                             │
                                             ▼
                                        ┌─────────┐
                                        │  MySQL  │
                                        └─────────┘
```

- **Frontend** — Vercel 배포, port 5173 (로컬)
- **Backend** — 별도 클라우드 배포, port 8080 (로컬)
- **Database** — MySQL

## API 연동 상태

| 영역 | 현재 | 목표 |
| --- | --- | --- |
| Frontend API | VibeX SDK | Spring Boot REST API |
| Frontend 배포 | VibeX 도메인 | Vercel |
| Backend | Spring Boot 초기 프로젝트 | Entity·API 구현 |

## 환경 변수

### Frontend

| 변수 | 설명 |
| --- | --- |
| `VITE_API_URL` | Spring API Base URL |

### Backend

| 변수 | 설명 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |

## 문서 링크

| 문서 | 내용 |
| --- | --- |
| [frontend/README.md](./frontend/README.md) | 프론트엔드 개요 |
| [frontend/SPEC.md](./frontend/SPEC.md) | 프론트엔드 상세 명세 |
| [backend/README.md](./backend/README.md) | 백엔드 개요 |
| [backend/SPEC.md](./backend/SPEC.md) | 백엔드 상세 명세 |
