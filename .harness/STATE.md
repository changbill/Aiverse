# STATE — 진행 상황 스냅샷

> 이 문서는 세션 로그가 **아니라** "지금까지 무엇이 끝났는가"의 단계 단위 요약이다.
> 세션별 서술(무엇을 했고 무엇이 막혔는지)은 `HANDOFF.md`에, 결정과 이유는 `DECISIONS.md`에 남긴다 — 여기에 다시 옮기지 않는다.
> 단계가 끝나면 그 단계를 한 줄로 갱신한다. 이슈/커밋 단위로 로그를 쌓지 않는다.

**마지막 갱신:** 2026-07-09

**AIverse MVP 구현 로드맵 9단계 전체 완료 + 미니PC 운영 배포 완료.**

## 완료된 단계

- **하네스 구성**: 크로스 툴(Claude Code/Codex/Cursor) 작업 연속성 하네스(`.harness/`, `CLAUDE.md`, `AGENTS.md`, `.cursor/rules/harness.mdc`) 구성. 프론트엔드/백엔드 모노레포 분리, 서비스 기획과 명세를 `ARCHITECTURE.md`로 통합.
- **1단계 백엔드 공통 기반**: Gradle 의존성(Security/JPA/Querydsl/Flyway/JWT/S3/Testcontainers), 프로필, Docker Compose, 공통 응답·오류 처리, Flyway V1·V2, Spring 관리 MySQLContainer와 테스트별 트랜잭션 롤백 기반 `IntegrationTestSupport`까지 전체 완료.
- **2단계 회원과 인증**: `User`/`RefreshToken` Entity+Repository(3계층), 회원가입·로그인·현재 사용자 조회, Swagger UI(springdoc), Access token 발급과 `SecurityFilterChain`, Refresh token 쿠키·해시 저장·회전·로그아웃, 입력 검증·인증 오류 테스트까지 전체 완료.
- **3단계 카테고리·태그·콘텐츠 탐색**: `Category`/`Tag`/`Asset`/`AssetTag` Entity+Repository(3계층), `GET /api/categories`·`GET /api/tags`, Querydsl 콘텐츠 검색·필터·정렬(`LATEST`/`POPULAR`/`PRICE_ASC`/`PRICE_DESC`)·페이지네이션, `GET /api/contents` 목록과 `GET /api/contents/{id}` 상세+`view_count` 증가, 비활성 카테고리 제외·태그 정규화·정렬 통합 테스트까지 전체 완료.
- **4단계 파일 업로드와 콘텐츠 관리**: S3/MinIO `ObjectStorageClient`(Presigned URL 발급)와 `POST /api/files/upload`(형식·용량 검증 후 사용자별 임시 객체 키 발급), `HEAD` 재검증을 포함한 `POST /api/contents` 등록, 소유자 전용 `PUT`/`DELETE /api/contents/{id}`(최초 판매 후 원본·라이선스 변경 409), 미등록 임시 객체 24시간 정리 스케줄러, 관련 경계 테스트까지 전체 완료.
- **5단계 크레딧과 목업 결제**: `CreditProduct`/`Payment`/`CreditTransaction` Entity+Repository(3계층), `GET /api/credit-products`(활성 상품만), `POST /api/payments`(서버 가격 기준 목업 결제, `Idempotency-Key` 필수·재요청 시 최초 결과 반환), 사용자 행 `PESSIMISTIC_WRITE` 잠금과 `READ_COMMITTED` 격리로 잔액·결제·거래 이력을 원자 처리, `GET /api/credit-transactions`(유형 필터·페이지네이션), 실제 스레드로 검증한 중복 충전·동시 요청 테스트까지 전체 완료.
- **6단계 구매·보관함·다운로드**: `Purchase` 쓰기 확장·`CreatorSettlement`/`Download` Entity+Repository(3계층), `POST /api/purchases`(구매자·창작자 행 사용자 ID 오름차순 잠금, 잔액 차감·80% 창작자 정산·`Purchase`/`CreatorSettlement` 생성 단일 트랜잭션, 본인 콘텐츠·중복 구매·잔액 부족 409, `Idempotency-Key` 재요청 처리), `GET /api/library`(삭제 콘텐츠도 계속 노출), `POST /api/downloads`(구매 확인 후 원본 Presigned GET URL 5분 발급), 실제 스레드로 검증한 교착 방지·가격 및 라이선스 스냅샷·삭제 콘텐츠 접근 테스트까지 전체 완료.
- **7단계 창작자 대시보드**: `GET /api/dashboard/sales`(`period=7D`/`30D`/`ALL`), `CreatorSettlement` 기준 Querydsl 집계로 등록 콘텐츠 수·누적 판매 횟수·크레딧(`totals`), 판매 없는 날짜를 0으로 채운 일자별 추이(`series`, `ALL`은 실제 판매일만), 판매량 상위 5개(동률 시 최신 콘텐츠 우선, `items`)까지 전체 완료. 다른 창작자 데이터 미혼입·기간 경계(정각 포함/직전 제외) 테스트 포함.
- **8단계 프론트엔드 REST API 전환**: (사전 준비로 `PUT /api/auth/me` 백엔드 엔드포인트 TDD 구현 포함) `VITE_API_URL` 기반 `httpClient`(공통 응답/오류 파싱, 401 시 재발급 훅)와 `authApi`/`contentApi`/`categoryApi`/`fileApi`/`creditApi`/`purchaseApi`/`dashboardApi` 도입, 메모리 Access token+Refresh 쿠키 재발급 기반 세션 복원(`useAppStore.restoreSession`)으로 통합해 기존 미사용 `AuthContext`/`AuthProvider`/`useAuth` 제거, Login·Register·Profile·Home·Explore·ContentDetail(`/content/:slug`→`/content/:id`)·Upload(Presigned 업로드+콘텐츠 등록)·Credits·Library·Dashboard(recharts 추이 차트) 전 페이지를 실제 백엔드 API로 교체, VibeX SDK(`src/sdk`, `src/api/{entities,vibexClient,integrations}.js`, `@vibexnpm/talkflow`)와 클라이언트가 직접 계산하던 mock 크레딧/구매/집계 로직 제거까지 전체 완료. (VibeX iframe 비주얼 에디터 개발 도구는 API 통신과 무관해 이번 범위에서 제외 — `BACKLOG.md` 참조)
- **9단계 전체 검증과 문서화**: 백엔드 `./gradlew test`(단위)·`./gradlew integrationTest`(Testcontainers MySQL 전체 통합)·`./gradlew build` 전체 통과, 프론트엔드 `npm run build` 프로덕션 빌드 성공까지 확인. Docker Compose(MySQL+MinIO) 환경에 로컬 프로필로 백엔드를 직접 기동해 회원가입→로그인→Presigned 업로드(커버+원본)→콘텐츠 등록→크레딧 충전→구매(80/20 정산)→보관함 조회→다운로드(원본 파일 바이트 일치 확인)→창작자 대시보드(판매 집계 반영 확인)까지 실제 API 호출로 전체 흐름을 검증했다(이 환경에 브라우저 도구가 없어 React UI 클릭 대신 curl 기반 API 흐름으로 검증 — `BACKLOG.md` 참조). 코드 수정 없이 전체 통과.
- **미니PC + Cloudflare Tunnel + Vercel 운영 배포**: 백엔드 `Dockerfile`(멀티스테이지)과 `docker-compose.yml`의 `app` 서비스로 미니PC에서 `docker compose up -d --build` 한 번에 MySQL+MinIO+API 기동. Cloudflare Tunnel 라우트 3개(API `9999`, MinIO S3 API `9998`, MinIO 콘솔 `9001`)를 공개 도메인에 연결하고 Vercel에 배포된 프론트엔드(`https://aiverse-blue.vercel.app`)와 CORS·Refresh 쿠키(`SameSite=None`)까지 연동 확인. `StorageProperties`/`StorageConfig`에 백엔드 내부용 `endpoint`(도커 네트워크)와 브라우저 노출용 `publicEndpoint`(Presigned URL)를 분리해 서버 자신의 HEAD 재검증이 프록시를 거치며 실패하던 문제를 해결. 실제 운영 도메인(`https://aiverse.changee.cloud`)으로 회원가입→업로드→충전→구매→다운로드→대시보드 전체 흐름을 curl로 검증 완료(브라우저 클릭 검증은 미수행 — `BACKLOG.md` 참조).

## 확립된 컨벤션 (요약 — 근거·배경은 `DECISIONS.md` 참조)

- Repository는 도메인 인터페이스 + `jpa`(Spring Data JPA) + `impl`(어댑터) 3계층. Service는 도메인 인터페이스에만 의존.
- 모든 백엔드 구현(Repository/Service/Controller)은 TDD(실패하는 테스트 우선 → 최소 구현 → 리팩터링).
- 테스트 실행: 기본은 단위 테스트만(`./gradlew test`). TDD로 통합 테스트 작성·수정 시 해당 테스트만 실행. Repository는 `@DataJpaTest` 슬라이스, 전체 스택은 `@SpringBootTest`. Testcontainers MySQL 재사용.
- Controller는 `ApiResponse<T>`/`PageResponse<T>`만 반환(`ResponseEntity` 금지, 비-200 상태는 `@ResponseStatus`), 라우팅만 담당하고 보조 로직은 `util`로 분리.
- 연관관계 N+1은 명시적 JPQL/Querydsl fetch join으로 해결(`@EntityGraph` 금지). 페이징 목록은 `XToOne`만 fetch join하고 목록·상세 DTO를 분리.
- Swagger UI(springdoc-openapi 3.0.3+) 제공 — Controller마다 `@Tag`/`@Operation`/(인증 필요 시) `@SecurityRequirement`.
- 비관적 쓰기 잠금 획득 후 같은 트랜잭션에서 조건을 다시 확인해야 하는 흐름(멱등키 재확인 등)은 기본 `REPEATABLE READ` 대신 `@Transactional(isolation = Isolation.READ_COMMITTED)`를 사용한다 — 그렇지 않으면 재확인 조회가 트랜잭션 시작 시점 스냅샷을 계속 사용해 동시 커밋을 못 본다.
- 비관적 쓰기 잠금(`findByIdForUpdate`)으로 조회할 User/행은, 같은 트랜잭션 안에서 그 이전에 fetch join 등으로 먼저 로드하지 않는다 — 이미 세션(영속성 컨텍스트)에 올라온 미잠금 인스턴스가 있으면 이후 잠금 쿼리가 DB 잠금은 정상 획득해도 Hibernate가 캐시된 자바 객체를 그대로 반환해 최신 값이 반영되지 않는다. 잠글 행과 연관된 엔티티(예: `Asset.creator`)는 fetch join하지 말고 ID만 얻는 조회로 분리한다.
- Querydsl `Expressions.dateTemplate`으로 MySQL `DATE(...)`를 조회할 때는 `LocalDate.class`가 아니라 `java.sql.Date.class`로 타입을 선언하고 `.toLocalDate()`로 변환한다 — JDBC가 `java.sql.Date`를 반환해 `LocalDate`로 바로 캐스팅하면 `ClassCastException`이 발생한다.
- 로컬 환경 변수는 `backend/.env`에 Docker Compose 변수와 Spring Boot 로컬 실행 변수를 함께 정리한다. `local`은 `${ENV:기본값}`으로 즉시 실행 가능하게 두고, `test`는 재현성을 위해 테스트 전용 고정값을 사용하며, `prod`는 환경 변수 주입을 필수로 한다.
- AIverse 백엔드 구현은 서브에이전트 팀 없이 단일 에이전트가 직접 수행 (`aiverse-backend-builder` 스킬 미사용).
- 소스 코드 변경 없는 순수 문서/설정 작업은 feature 브랜치 없이 master에 바로 커밋.
- 브랜치 전략: `PLAN.md` 단계 하나당 브랜치 하나, 체크리스트 항목 하나당 커밋 하나 (자세한 내용은 `CLAUDE.md`의 "하네스: 브랜치 전략" 참조).

## 다음 단계

AIverse MVP 구현 로드맵 9단계와 미니PC 운영 배포까지 전체 완료. 다음 계획은 미정 — 새 요청 시 `PLAN.md`에 초안 작성 후 진행 (`BACKLOG.md`의 기술 부채·향후 아이디어 참조).
