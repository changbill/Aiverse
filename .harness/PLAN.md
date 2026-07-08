# PLAN — 현재 구현 계획

> 새 기능/변경을 시작하기 전에 계획 초안을 작성하고 사용자 피드백을 받아 확정한 뒤 구현한다.
> 이 문서에는 제안·확정·진행 중인 계획을 유지한다.
> 구현이 완료되면 결과를 `STATE.md` 등 해당 산출물에 반영하고 완료된 계획은 이 문서에서 제거한다.
> **브랜치/커밋 단위:** 이 문서의 단계 하나 = feature 브랜치 하나, 체크리스트 항목 하나 = 커밋 하나 (`CLAUDE.md`의 "하네스: 브랜치 전략" 참조).

## 현재 계획

### 미니PC + Cloudflare Tunnel + Vercel 운영 배포 — (확정)

**배경:** 미니PC에 백엔드(API+MySQL+MinIO)를 `docker compose up -d`로 띄우고 Cloudflare Tunnel로 공개 도메인을 연결해, Vercel에 이미 배포된 프론트엔드(`https://aiverse-blue.vercel.app`)가 실제로 통신할 수 있게 한다. Cloudflare Tunnel 라우트 3개가 준비되어 있다: `aiverse.changee.cloud`→`localhost:9999`(백엔드 API), `aiverse-storage.changee.cloud`→`localhost:9998`(MinIO S3 API), `aiverse-storage-admin.changee.cloud`→`localhost:9001`(MinIO 관리 콘솔). (다단계 서브도메인 `aiverse.storage.*`/`aiverse.storage.admin.*`은 Cloudflare 기본 Universal SSL 와일드카드 인증서가 한 단계까지만 커버해 TLS 핸드셰이크가 실패하는 문제를 겪어, 한 단계짜리 하이픈 서브도메인으로 변경했다 — `DECISIONS.md` 참조.)
**범위:** 백엔드 앱 컨테이너화, docker-compose 포트/서비스 구성, 운영용 환경변수 정리(새 MinIO 자격증명 발급 포함), 미니PC 배포, 배포 환경 전체 흐름 검증까지 포함한다. Cloudflare Tunnel 설치·라우트 설정과 Vercel 프로젝트 생성·배포는 사용자가 이미 완료했으므로 포함하지 않는다(단, Vercel의 `VITE_API_URL` 환경변수 확인/안내는 포함). CI/CD 자동화는 포함하지 않는다(수동 배포).
**단계:**

- [ ] 미니PC에 배포(`git pull` + `docker compose up -d --build`)하고 Cloudflare Tunnel 세 라우트가 외부에서 정상 응답하는지 확인
- [ ] Vercel 프로젝트의 `VITE_API_URL=https://aiverse.changee.cloud` 환경변수 설정 확인 안내
- [ ] `https://aiverse-blue.vercel.app`에서 회원가입→업로드→충전→구매→다운로드→대시보드 전체 흐름을 브라우저로 검증
      **영향받는 영역:** 둘 다

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
