# PLAN — 현재 구현 계획

> 새 기능/변경을 시작하기 전에 계획 초안을 작성하고 사용자 피드백을 받아 확정한 뒤 구현한다.
> 이 문서에는 제안·확정·진행 중인 계획을 유지한다.
> 구현이 완료되면 결과를 `STATE.md` 등 해당 산출물에 반영하고 완료된 계획은 이 문서에서 제거한다.

## 현재 계획

### AIverse MVP 구현 로드맵 — (제안)

**배경:** 합의된 아키텍처와 API 계약을 실제 코드로 옮기기 위해 공통 기반부터 거래 기능과 프론트엔드 전환까지 의존 순서대로 구현한다.
**범위:** Spring Boot REST API, MySQL·MinIO 로컬 환경, 통합 테스트, VibeX SDK 제거 및 React 연동을 포함한다. 외부 실결제, 미디어 변환, 관리자 API, 이메일 인증은 포함하지 않는다.
**단계:**

#### 1단계: 백엔드 공통 기반

- [ ] Gradle에 Security, Validation, JPA, Querydsl, Flyway, MySQL, JWT, S3 SDK, Testcontainers 의존성 구성
- [ ] `local`·`test`·`prod` 설정과 환경 변수 바인딩 구성
- [ ] Docker Compose MySQL·MinIO 로컬 실행 환경 구성
- [ ] 공통 `{data}`·`{data,page}` 응답, 오류 응답, 예외 처리, `requestId` 구성
- [ ] 초기 Flyway 스키마·인덱스·카테고리·크레딧 상품 seed 작성
- [ ] Testcontainers MySQL 기반 통합 테스트 공통 설정 및 애플리케이션 기동 검증

#### 2단계: 회원과 인증

- [ ] User·RefreshToken 엔티티와 Repository 구현
- [ ] 회원가입·로그인·현재 사용자 조회 구현
- [ ] Access token 발급과 Security 인증 필터 구현
- [ ] Refresh token 쿠키·해시 저장·회전·로그아웃 구현
- [ ] 입력 검증, 중복 이메일·닉네임, 비활성 계정, 인증 오류 테스트

#### 3단계: 카테고리·태그·콘텐츠 탐색

- [ ] Category·Tag·Asset·AssetTag 엔티티와 조회 Repository 구현
- [ ] 카테고리 목록과 태그 검색·사용량순 목록 API 구현
- [ ] Querydsl 기반 콘텐츠 검색·필터·동적 정렬·페이지네이션 구현
- [ ] 콘텐츠 상세 조회와 `view_count` 증가 구현
- [ ] 비활성 카테고리 제외, 태그 정규화, 인기순·가격순·최신순 통합 테스트

#### 4단계: 파일 업로드와 콘텐츠 관리

- [ ] S3·MinIO 공통 Object Storage 어댑터와 Presigned URL 구현
- [ ] 형식·용량 검증 후 사용자별 임시 객체 키를 발급하는 업로드 API 구현
- [ ] `HEAD` 검증을 포함한 콘텐츠 등록 API 구현
- [ ] 소유자 전용 콘텐츠 수정·소프트 삭제와 최초 판매 후 변경 제한 구현
- [ ] 미등록 임시 객체 24시간 정리 작업 구현
- [ ] 파일 검증·소유권·상태 전이·변경 제한 테스트

#### 5단계: 크레딧과 목업 결제

- [ ] CreditProduct·Payment·CreditTransaction 엔티티와 Repository 구현
- [ ] 활성 크레딧 상품 조회 API 구현
- [ ] 서버 가격 기준 목업 결제와 `Idempotency-Key` 재요청 처리 구현
- [ ] 사용자 잔액·결제·거래 이력 단일 트랜잭션 처리 구현
- [ ] 크레딧 거래 이력 조회 API와 중복 충전·동시 요청 테스트

#### 6단계: 구매·보관함·다운로드

- [ ] Purchase·CreatorSettlement 엔티티와 Repository 구현
- [ ] 사용자 행 ID 순서 잠금, 잔액 차감, 창작자 정산, 구매 기록 단일 트랜잭션 구현
- [ ] 본인 콘텐츠·중복 구매·잔액 부족·`Idempotency-Key` 처리 구현
- [ ] 구매 보관함 조회 API 구현
- [ ] 구매 권한 확인, 다운로드 기록, 원본 Presigned URL 발급 구현
- [ ] 동시 구매·교착 방지·가격 및 라이선스 스냅샷·삭제 콘텐츠 접근 테스트

#### 7단계: 창작자 대시보드

- [ ] `7D`·`30D`·`ALL` 기간 조건과 총계 집계 구현
- [ ] 일자별 `series`와 판매 상위 5개 `items` 집계 구현
- [ ] 다른 사용자의 판매 데이터가 섞이지 않는 소유권·기간 경계 테스트

#### 8단계: 프론트엔드 REST API 전환

- [ ] `VITE_API_URL` 기반 HTTP 클라이언트와 공통 응답·오류 처리 구현
- [ ] 메모리 Access token, Refresh cookie 재발급, 인증 상태 복원 구현
- [ ] Login·Register·Profile 페이지를 Auth API에 연동
- [ ] Home·Explore·ContentDetail을 콘텐츠·카테고리·태그 API에 연동
- [ ] Upload를 Presigned 업로드와 콘텐츠 등록 흐름에 연동
- [ ] Credits를 상품·결제·거래 이력 API에 연동
- [ ] Library·다운로드·구매 흐름을 Purchase API에 연동
- [ ] Dashboard를 기간별 판매 통계 API에 연동
- [ ] VibeX SDK 의존 코드와 신뢰할 수 없는 클라이언트 계산 제거

#### 9단계: 전체 검증과 문서화

- [ ] 백엔드 전체 테스트와 Gradle 빌드 수행
- [ ] 프론트엔드 프로덕션 빌드 수행
- [ ] Docker Compose 환경에서 회원가입→업로드→충전→구매→다운로드→대시보드 흐름 검증
- [ ] 구현된 동작을 `SPEC.md`, 실행 방법을 `README.md`, 완료 단계 결과를 `.codex/markdown/`에 반영

**영향받는 영역:** frontend / backend / `README.md` / `SPEC.md` / `.codex/markdown/` / `.harness`

## 계획 작성 형식

```markdown
### {기능/작업명} — (제안 | 확정 | 진행중)

**배경:** 왜 필요한가
**범위:** 무엇을 하고, 무엇을 하지 않는가
**단계:**
- [ ] 단계 1
- [ ] 단계 2
**영향받는 영역:** frontend / backend / 둘 다
```
