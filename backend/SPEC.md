# Backend 명세

## Tech Stack

| 항목 | 버전 / 도구 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Web | Spring Web MVC |
| ORM | Spring Data JPA |
| Database | MySQL |
| Build | Gradle 9.5.1 (Wrapper 포함) |
| Utility | Lombok |

## 폴더 구조

```
backend/
├── build.gradle             # 의존성 및 빌드 설정
├── settings.gradle          # 프로젝트명 (aiverse)
├── gradlew / gradlew.bat    # Gradle Wrapper
├── gradle/wrapper/
└── src/
    ├── main/
    │   ├── java/com/example/aiverse/
    │   │   └── AiverseApplication.java    # 진입점
    │   └── resources/
    │       └── application.yaml           # 앱 설정
    └── test/
        └── java/com/example/aiverse/
            └── AiverseApplicationTests.java
```

### 구현 예정 패키지 구조

```
com.example.aiverse/
├── config/          # CORS, Security, JPA 설정
├── controller/      # REST API 엔드포인트
├── service/         # 비즈니스 로직
├── repository/      # JPA Repository
├── entity/          # JPA Entity
├── dto/             # Request / Response DTO
└── exception/       # 예외 처리
```

## Gradle Commands

| Command | Description |
| --- | --- |
| `./gradlew bootRun` | 개발 서버 실행 (port 8080) |
| `./gradlew build` | 컴파일 및 테스트 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew bootJar` | 실행 가능 JAR 생성 |

## 설정

`application.yaml` 현재 상태:

```yaml
spring:
  application:
    name: aiverse
```

MySQL 연동 시 추가 예정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiverse
    username: ...
    password: ...
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
```

## DB 스키마

### user

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| email | varchar | unique |
| password | varchar | |
| nickname | varchar | |
| role | varchar | |
| profile_url | varchar | nullable |
| introduction | text | nullable |
| credit_balance | int | |
| created_at | datetime | |
| updated_at | datetime | nullable |

### asset

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| creator_id | bigint | FK → user |
| title | varchar | |
| description | varchar | nullable |
| asset_type | varchar | image / video / music |
| category | varchar | |
| preview_url | varchar | nullable |
| file_url | varchar | |
| price_credit | int | |
| ai_tool | varchar | nullable |
| license_type | varchar | |
| created_at | datetime | |
| updated_at | datetime | nullable |

### tag

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| name | varchar | unique |
| created_at | datetime | |

### asset_tag

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| asset_id | bigint | FK → asset |
| tag_id | bigint | FK → tag |

### credit_product

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| name | varchar | |
| credit_amount | int | |
| bonus_credit | int | |
| price | int | |
| status | varchar | |
| created_at | datetime | |

### payment

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | FK → user |
| credit_product_id | bigint | FK → credit_product |
| amount | int | |
| method | varchar | |
| status | varchar | |
| transaction_key | varchar | nullable |
| paid_at | varchar | nullable |
| failed_reason | varchar | nullable |
| created_at | datetime | |

### credit_transaction

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | FK → user |
| payment_id | varchar | nullable |
| type | varchar | |
| amount | int | |
| balance_after | int | |
| reason | varchar | |
| created_at | datetime | |

### purchase

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | FK → user |
| asset_id | bigint | FK → asset |
| credit_transaction_id | bigint | FK → credit_transaction |
| purchase_price_credit | int | |
| license_type | varchar | |
| purchased_at | datetime | |

### download

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| purchase_id | bigint | FK → purchase |
| user_id | bigint | FK → user |
| asset_id | bigint | FK → asset |
| downloaded_at | datetime | |

### creator_settlement

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| creator_id | bigint | FK → user |
| purchase_id | bigint | FK → purchase |
| asset_id | bigint | FK → asset |
| gross_credit | int | |
| platform_fee_credit | int | |
| settlement_credit | int | |
| status | varchar | |
| created_at | datetime | |
| settled_at | datetime | nullable |

## API 명세 (구현 예정)

### Auth

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → JWT 반환 |
| GET | `/api/auth/me` | 현재 사용자 정보 |

### Asset (Content)

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/contents` | 목록 (페이지·필터·정렬) |
| GET | `/api/contents/{id}` | 상세 |
| POST | `/api/contents` | 등록 (창작자) |
| PUT | `/api/contents/{id}` | 수정 |
| DELETE | `/api/contents/{id}` | 삭제 |

### Category / Tag

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/categories` | 카테고리 목록 |
| GET | `/api/tags` | 태그 목록 |

### Credit / Payment

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/credit-products` | 충전 상품 목록 |
| POST | `/api/payments` | 결제 요청 (목업) |
| GET | `/api/credit-transactions` | 크레딧 거래 이력 |

### Purchase / Library

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/purchases` | 콘텐츠 구매 |
| GET | `/api/library` | 구매 보관함 |
| POST | `/api/downloads` | 다운로드 기록 |

### Dashboard

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/dashboard/sales` | 창작자 판매 통계 |

### File

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/files/upload` | 파일 업로드 |

## CORS

프론트엔드 도메인에서 API 호출을 허용해야 합니다.

```
http://localhost:5173          # 로컬 개발
https://*.vercel.app           # Vercel 배포
```

## DDL (참고)

<details>
<summary>전체 CREATE TABLE 문</summary>

```sql
CREATE TABLE `asset` (
	`id` bigint NOT NULL,
	`creator_id` bigint NOT NULL,
	`title` varchar NOT NULL,
	`description` varchar NULL,
	`asset_type` varchar NOT NULL,
	`category` varchar NOT NULL,
	`preview_url` varchar NULL,
	`file_url` varchar NOT NULL,
	`price_credit` int NOT NULL,
	`ai_tool` varchar NULL,
	`license_type` varchar NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NULL
);

CREATE TABLE `credit_product` (
	`id` bigint NOT NULL,
	`name` varchar NULL,
	`credit_amount` int NULL,
	`bonus_credit` int NULL,
	`price` int NULL,
	`status` varchar NULL,
	`created_at` datetime NULL
);

CREATE TABLE `user` (
	`id` bigint NOT NULL,
	`email` varchar NOT NULL COMMENT 'unique',
	`password` varchar NOT NULL,
	`nickname` varchar NOT NULL,
	`role` varchar NOT NULL,
	`profile_url` varchar NULL,
	`introduction` text NULL,
	`credit_balance` int NOT NULL,
	`created_at` datetime NOT NULL,
	`updated_at` datetime NULL
);

CREATE TABLE `download` (
	`id` bigint NOT NULL,
	`purchase_id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`downloaded_at` datetime NOT NULL
);

CREATE TABLE `asset_tag` (
	`id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`tag_id` bigint NOT NULL
);

CREATE TABLE `credit_transaction` (
	`id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`payment_id` varchar NULL,
	`type` varchar NOT NULL,
	`amount` int NOT NULL,
	`balance_after` int NOT NULL,
	`reason` varchar NOT NULL,
	`created_at` datetime NOT NULL
);

CREATE TABLE `purchase` (
	`id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`credit_transaction_id` bigint NOT NULL,
	`purchase_price_credit` int NOT NULL,
	`license_type` varchar NOT NULL,
	`purchased_at` datetime NOT NULL
);

CREATE TABLE `creator_settlement` (
	`id` bigint NOT NULL,
	`creator_id` bigint NOT NULL,
	`purchase_id` bigint NOT NULL,
	`asset_id` bigint NOT NULL,
	`gross_credit` int NOT NULL,
	`platform_fee_credit` int NOT NULL,
	`settlement_credit` int NOT NULL,
	`status` varchar NOT NULL,
	`created_at` datetime NOT NULL,
	`settled_at` datetime NULL
);

CREATE TABLE `payment` (
	`id` bigint NOT NULL,
	`user_id` bigint NOT NULL,
	`credit_product_id` bigint NOT NULL,
	`amount` int NOT NULL,
	`method` varchar NOT NULL,
	`status` varchar NOT NULL,
	`transaction_key` varchar NULL,
	`paid_at` varchar NULL,
	`failed_reason` varchar NULL,
	`created_at` datetime NOT NULL
);

CREATE TABLE `tag` (
	`id` bigint NOT NULL,
	`name` varchar NOT NULL COMMENT 'unique',
	`created_at` datetime NOT NULL
);
```

</details>
