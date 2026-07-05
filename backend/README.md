# Backend

Aiverse REST API 서버입니다. 사용자 인증, AI 창작물 관리, 크레딧 결제, 구매·다운로드 권한 등 핵심 비즈니스 로직을 처리합니다.

## 역할

프론트엔드가 직접 DB에 접근하지 않고, 백엔드 API를 통해 모든 데이터를 주고받습니다.

- **인증** — 회원가입, 로그인, JWT 기반 세션 관리
- **콘텐츠(Asset)** — AI 창작물 CRUD, 카테고리·태그 관리
- **크레딧** — 충전(목업 결제), 잔액 조회, 거래 이력
- **구매** — 크레딧 차감, 구매 권한 생성, 다운로드 이력
- **정산** — 창작자 판매 수익 기록

## 도메인 개요

| 엔티티 | 설명 |
| --- | --- |
| User | 사용자 (창작자 / 구매자) |
| Asset | AI 창작물 (이미지, 영상, 음악) |
| Tag / AssetTag | 태그 및 콘텐츠-태그 연결 |
| CreditProduct | 크레딧 충전 상품 |
| Payment | 결제 이력 (목업) |
| CreditTransaction | 크레딧 증감 이력 |
| Purchase | 콘텐츠 구매 이력 |
| Download | 다운로드 이력 |
| CreatorSettlement | 창작자 정산 |

## 기술 개요

Java 21 + Spring Boot 4.1 + Spring Data JPA + MySQL + Gradle로 구성됩니다.

현재 Spring Boot 초기 프로젝트 상태이며, Entity·Controller·Service 계층과 DB 연동을 순차적으로 구현할 예정입니다.

## Getting Started

```bash
./gradlew bootRun        # Linux / macOS
gradlew.bat bootRun      # Windows
```

API 서버는 **http://localhost:8080** 에서 실행됩니다.

MySQL 연결 설정은 `src/main/resources/application.yaml`에서 관리합니다.

## 문서

- [../.harness/ARCHITECTURE.md](../.harness/ARCHITECTURE.md) — 폴더 구조, DB 스키마, Gradle 명령, API 명세
