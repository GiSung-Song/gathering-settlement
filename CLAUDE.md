# 프로젝트 개요
모임 정산 앱. 여러 명이 결제한 비용을 "누가 누구에게 얼마"로 자동 정산한다.
개인 학습용 프로젝트로, AI 기반 개발 자동화와 LLM 기능 연동을 연습한다.
- 기획·용어·엣지 케이스·로드맵: docs/PROJECT.md (기능 작업 전 반드시 확인)
- docs/PROJECT.md의 "미결정" 항목은 임의로 정하지 말고 먼저 질문한다.

# 기술 스택
- Backend: Java 21, Spring Boot 4.x, Gradle
- DB: MySQL 8, Spring Data JPA (기본), MyBatis (복잡한 조회/통계 쿼리에만)
- Migration: Flyway (src/main/resources/db/migration)
- Frontend: React + TypeScript (Vite)
- Test: JUnit 5, AssertJ, Testcontainers(MySQL), Vitest

# 디렉터리 구조
- backend/  : Spring Boot 프로젝트
- frontend/ : React 프로젝트
- docker-compose.yml : 로컬 MySQL

# 주요 명령어
- 백엔드 테스트: cd backend && ./gradlew test
- 백엔드 실행:   cd backend && ./gradlew bootRun
- 프론트 테스트: cd frontend && npm test
- 로컬 DB:       docker compose up -d

# 백엔드 규칙
- 계층: controller → service → repository. controller에 비즈니스 로직 금지.
- Entity를 API 응답으로 직접 노출하지 않는다. Request/Response DTO 사용.
- @Transactional은 service 계층에만. 조회 메서드는 readOnly = true.
- 연관관계는 기본 LAZY. N+1이 생기면 fetch join 또는 @EntityGraph로 해결.
- MyBatis는 JPA로 표현이 어려운 조회에만 사용하고, 쓰기는 JPA로 한다.
- 스키마 변경은 반드시 Flyway 스크립트로. ddl-auto는 validate.

# 테스트 규칙
- 기능/버그 수정 시 테스트를 먼저 작성하고 실패를 확인한 뒤 구현한다.
- Repository 테스트는 @DataJpaTest + Testcontainers(MySQL). H2 사용 금지.
- "완료"라고 말하기 전에 ./gradlew test 를 실제로 실행하고 결과를 보여준다.

# 작업 방식
- 코드를 쓰기 전에 가정과 해석을 먼저 말하고, 애매하면 질문한다.
- 가장 단순한 방법을 우선한다. 요청하지 않은 추상화·기능은 추가하지 않는다.
- 요청받은 부분만 수정한다. 주변 코드 정리·포맷 변경 금지.
- 변경은 한 번에 하나씩 제안한다. 각 변경마다:
  파일 / 메서드 / 기존 → 변경 / 이유 / 코드

# 커밋 메시지 (= 작업 기록)
- 별도 기록 파일은 두지 않는다. 커밋 메시지가 작업 기록이다.
- 커밋은 구현과 검증이 모두 끝난 뒤 작업 단위로 한 번 한다.
- 검증을 실제로 하지 않았으면 "검증: 미검증"이라고 적는다. 추측으로 채우지 않는다.

형식:
<type>: <제목 한 줄>

내용: 무엇을 왜 바꿨는지 1~3줄
검증: 실행한 명령어·테스트와 결과

type: feat(기능 추가) / fix(버그 수정) / refactor(개선) / test(테스트) / docs(문서) / chore(설정)

예시:
feat: 정산 잔액 계산 로직 추가

내용: 멤버별 (낸 돈 − 쓴 돈) 잔액 계산. 잔액 합계 0 검증 포함.
검증: ./gradlew test 통과 (SettlementCalculatorTest 4건)
