[33mcommit 2bc0b3fd194aa29013cda4012a264159638e070a[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mfix/#107/add-front-cors[m[33m, [m[1;31morigin/fix/#107/add-front-cors[m[33m)[m
Author: pywoo <kfdsy0103@naver.com>
Date:   Sun Feb 1 17:43:48 2026 +0900

    fix: 연동 테스트를 위한 oauth2 redirect 주소 5173 변경

[33mcommit fbddbebaf1d79a51e337b6a3e0b88ed5186cd2c2[m
Author: junho <2171168@hansung.ac.kr>
Date:   Sun Feb 1 17:41:30 2026 +0900

    fix: cors 설정

[33mcommit d77e96eef48216a322dd6b87ad2efc69c7e15721[m[33m ([m[1;31morigin/feat/#106/evaluation-my[m[33m, [m[1;31morigin/develop[m[33m, [m[1;32mdevelop[m[33m)[m
Merge: 1c7806d 2eb2cc7
Author: pywoo <kfdsy0103@naver.com>
Date:   Sat Jan 31 20:36:51 2026 +0900

    fix: merge conflict

[33mcommit 2eb2cc796bd09df4584da8a00a534751c3a51260[m[33m ([m[1;31morigin/main[m[33m)[m
Author: Yongwoo Park <81312085+kfdsy0103@users.noreply.github.com>
Date:   Sat Jan 31 20:34:43 2026 +0900

    fix: cors hotfix
    
    * feat: set time zone asia/seoul
    
    * feat: add patch method to cors
    
    * fix: cors
    
    * fix: add hotfix/** to deploy.yml for test
    
    * fix: cors() 누락
    
    * fix: deploy yaml에서 hotfix/** 제거

[33mcommit 05a489b2325f46999ea9c6045f6551cfcca0fa3f[m
Author: Yongwoo Park <81312085+kfdsy0103@users.noreply.github.com>
Date:   Fri Jan 30 19:19:52 2026 +0900

    release: develop > main 1차 릴리즈
    
    * fix: stderr 파이프라인 구문 오류 수정
    
    * fix: 테스트 브랜치 배포 트리거 제거
    
    * infra: CI Gradle 로그 최소화
    
    * feat: 스터디 생성 API 구현
    
    * refactor: 코드래빗 리뷰 반영
    
    * fix: Docs에 responseCode 추가
    
    * fix: 스터디 생성 시 방장을 자동으로 STUDY_LEADER 및 멤버로 등록하는 로직 추가
    
    * feat: 스터디 역할에 따른 스터디 목록 조회 API
    
    * refactor: 중복 정책에 맞게 toMap merge 로직 제거
    
    * fix: GrowthLog 에러 코드 문자열과 HTTP 상태 코드 정합성 수정
    
    * fix: AiServerProperties에 @NotBlank 검증 추가
    
    * fix: 성장로그 재시도 동시성 방지
    
    * fix: 성장 로그 재시도 API 응답 상태 코드 수정
    
    * fix: 설정 검증 도입으로 baseUrl null 체크 제거
    
    * fix: ci 환경에서 Fake AI 클라이언트 활성화
    
    * fix: @Transactional 제거
    
    * fix: 스터디 생성 성공응답 _OK -> _CREATED 변경
    
    * refactor: 평가하기 버튼 활성화 로직 코드 리팩터링
    
    * fix: enum과 동일한 네이밍으로 변경
    
    * feat: 채용공고 추천 및 동적 필터 검색 API 구현 (#51)
    
    * feat: 마이페이지 프로필 조회
    
    * refactor: 포매팅
    
    * feat: 내 정보 조회 api 임시구현
    
    * feat: 모집 분야에 전공 필드 추가
    
    * fix: 근무지 workplace 네이밍 변경
    
    * feat: 추천 공고 응답 DTO 정의
    
    * feat: link 필드 추가
    
    * feat: ability repository 생성
    
    * fix: 타입 안정성을 위해 float[]를 Vector로 변경
    
    * feat: embedding 테이블 분리 및 지연로딩
    
    * feat: user 엔티티에 학력, 경력, 전공 필드 추가
    
    * feat: cursor, page 공통 응답 정적 메서드 패턴 적용
    
    * feat: positionkpi가 임베딩 값을 지연로딩으로 갖도록 설정
    
    * feat: AuthUser 스웨거 UI에서 숨김
    
    * fix: 버전 문제로 임베딩 필드 float[]로 수정
    
    * feat: 사용자 추천 공고 조회 API 구현
    
    * build: QueryDSL 의존성 추가
    
    * feat: QueryDSL config 작성
    
    * feat: 경력 및 학력에 order 필드 추가
    
    * feat: position 엔티티 시작일 마감일 필드 추가
    
    * refactor: querydsl 처리
    
    * chore: .gitignore querydsl
    
    * refactor: 조회 서비스 코드 변경
    
    * feat: 스웨거 명세 작성
    
    * chore: 메서드 네이밍 변경
    
    * fix: summary 필드명 수정
    
    * feat: 학력 이넘, 전공 엔티티 추가 & 매핑 테이블 생성
    
    * feat: 학과 전체 조회
    
    * refactor: dto클래스 레코드로 변경, Converter 구현체로 변경 및 ConversionService사용
    
    * feat: 마이페이지 조회(내정보) API 최종
    
    * docs: 프로필 조회 스웨거 설명 추가
    
    * refactor: department 패키지 분리
    
    * chore: 기존 내정보 조회 코드 삭제
    
    * refactor: 컨버터 제거
    
    * refactor: 유저전공 조회시 엔티티 -> 전공명 반환
    
    * chore: 오타 수정
    
    * refactor: 코드래빗 리뷰 반영
    
    * build: jdk 21 변경
    
    * chore: board 임시 삭제
    
    * chore: userdepartment 불필요 import 삭제
    
    * fix: education level 엔티티로 통일
    
    * fix: 변경사항에 맞게 조회 메서드 수정
    
    * feat: KPI 카드로 추천 공고 조회 API 구현
    
    * feat: kpi 카드 관련 공고검색 API docs 작성
    
    * chore: recruitment 도메인 패키지 구조 정리
    
    * feat: 합산을 구하기 위해 프로젝션 방식 변경
    
    * chore: dto 네이밍 수정
    
    * fix: recruitment 네이밍 및 코드 수정
    
    * fix: projection 네이밍 변경
    
    * feat: position dto 정의
    
    * feat: position repository 작성
    
    * chore: recruitment 메서드 네이밍 수정
    
    * feat: 유저 및 전공 검색 목적 fetch join 쿼리 작성
    
    * feat: recruitment 및 job에 batchsize 적용
    
    * feat: position 조회 서비스 및 컨트롤러 작성 완료
    
    * feat: 전체 공고 개수 조회 API 구현
    
    * fix: ability 도메인 네이밍 수정 및 mapsid 누락
    
    * refactor: 유저 및 전공 검색 시 n+1 처리
    
    * fix: position 조회 쿼리를 전체 검색 의도에 맞게 수정
    
    * chore: 불필요 import 삭제
    
    * fix: 코사인 쿼리 수정
    
    * feat: position docs 추가 설명
    
    * feat: position docs 추가 설명
    
    * fix: booleanExpression stream 시 NPE 방지
    
    * fix: 누락된 dto 필드 추가
    
    * chore: dto 띄어쓰기 변경
    
    * fix: docs 누락 어노테이션 추가
    
    * refactor: 최소 매칭 KPI 리뷰 반영
    
    * fix: 불필요한 fetch join 삭제
    
    * fix: 코드래빗 리뷰 반영
    
    * fix: 코드래빗 리뷰 반영
    
    ---------
    
    Co-authored-by: junho <2171168@hansung.ac.kr>
    
    * fix: hasNext 판단 및 결과 처리 수정
    
    * fix: KPI 카드 존재하지 않는 경우 에러 코드 추가
    
    * fix: 스터디 생성 시 직무에 따른 KPI 카드 선택 추가
    
    * feat: 스터디 생성 시 직무별 KPI 카드 조회 API 구현
    
    * docs: 스터디 Swagger 문서화 추가
    
    * fix: 스터디 목록 조회 시 각 스터디에 해당하는 KPI 카드 이름 추가
    
    * fix: 코드래빗 리뷰기반 수정
    
    * feat: 채용 공고 dto 정의
    
    * feat: positionkpi 및 kpiembedding repository 생성
    
    * feat: 공고 dto to entity 작성
    
    * feat: positionkpi 컨버터 작성
    
    * feat: positionkpiembedding 컨버터 작성
    
    * fix: dto 구조 변경
    
    * feat: position toEntity 작성
    
    * feat: label로 JobType을 찾는 메서드 추가
    
    * feat: position batch insert 작성
    
    * chore: 패키지 구조 변경
    
    * feat: QueryDSL 사용해서 약점 KPI와 스터디 KPI 매핑하여 스터디 추천해주는 repository 작성
    
    * feat: 맞춤형 스터디 추천 목록 조회 API 구현
    
    * docs: 스터디 Swagger 문서화 추가
    
    * chore: 변경된 패키지에 맞게 import
    
    * feat: positionKpi batch insert 작성
    
    * docs: 스터디 Swagger 문서화 추가
    
    * fix: where절에 승인수락한 인원만 카운트되는 코드 추가
    
    * build: postgresql runtime > implementation 변경
    
    * feat: positionKpiEmbedding batch insert 작성
    
    * feat: batch insert repository 상속
    
    * feat: 채용 공고 저장 로직 구현
    
    * fix: 유저의 가입/신청 목록을 조회할 수 있는 findByUserId 메서드 추가
    
    * fix: findById -> findByUserId로 변경
    
    * feat: redis config 작성
    
    * feat: application.yml에 stream key, group 추가
    
    * chore: key, group 네이밍 수정
    
    * feat: recruitment consumer 작성
    
    * feat: board에 builder default 추가
    
    * fix: 누락된 import 추가
    
    * feat: 채용 공고 중복 등록 처리
    
    * chore: 채용 공고 컨슈머 주석 및 로그 추가
    
    * feat: position 배치 처리 시 null 처리
    
    * fix: craetedAt, updatedAt 시간 관련 처리
    
    * feat: stream pending 메시지 처리 스케쥴러 작성
    
    * chore: 코드 줄바꿈
    
    * test: pending 처리 스케쥴러 테스트
    
    * fix: objectMapper readValue로 수정
    
    * build: 문장 임베딩을 위한 spring ai 의존성 추가
    
    * feat: yml에 openai apikey 추가
    
    * feat: embedding client 작성
    
    * feat: position 엔티티에 id 수동 할당을 위한 메서드 추가
    
    * fix: embedding을 main api에서 처리하도록 수정
    
    * fix: 스케쥴러에서 스트림 처리 완료 시 ACK 누락 수정
    
    * feat: 컨슈머 적재 오류 발생 시 errorCount 추가
    
    * fix: 변경된 DTO에 맞게 수정
    
    * feat: 컨슈머와 스케쥴러의 동시 처리 문제 방지
    
    * feat: IDENTITY 전략에서 Batch Insert시 pk가 필요한 경우 처리
    
    * fix: positionkpi repository 잘못된 타입 상속 해결
    
    * feat: PM,DESIGN,FRONT,BACK 과 관련없는 공고 처리
    
    * feat: stream 관련 빈에 profile 제한
    
    * fix: 스케쥴러 주기 5분으로 변경
    
    * feat: areaType에 대한 JsonCreator 작성
    
    * feat: companySize에 대한 JsonCreator 작성
    
    * feat: employmentType에 대한 JsonCreator 작성
    
    * feat: experienceType에 대한 JsonCreator 작성
    
    * feat: industryType에 대한 JsonCreator 작성
    
    * feat: jobType에 대한 JsonCreator 작성
    
    * feat: majorType에 대한 JsonCreator 작성
    
    * feat: position 컨트롤러 docs 설명 추가
    
    * chore: ddl-auto 위치 변경
    
    * fix: 게시물 생성 시 초기 좋아요 수 0으로 초기화
    
    * fix: 게시글 생성 시 성공 코드 _CREATED로 변경
    
    * fix: 알고리즘 오류 수정
    
    * fix: level 컬럼 추가
    
    * feat: 추천 채용 공고 체크 타임 추가
    
    * feat: AI 평가 컨텍스트에 포트폴리오 제목/내용 포함
    
    * test: AI 평가 컨텍스트에 포트폴리오 및 최근 성장로그 포함 검증
    
    * test: AI 평가 예외 발생 시 성장 로그 FAILED 저장 검증
    
    * test: 다수의 최근 성장로그 전달 및 KPI 링크 조회 검증
    
    * refactor: 성장 로그 평가 서비스 테스트 코드 함수 분리
    
    * fix: softDelete 적용 및 댓글 삭제 시 '삭제되었습니다'로 내용 변경
    
    * fix: PageResponseDto -> CursorResponseDto 변경
    
    * fix: swagger 문서화 수정
    
    * fix: repository 네이밍 변경
    
    * fix: 재시도 중 컨텍스트 생성 실패 시 상태 복구 처리
    
    * fix: 원본 내용 유지하는 방향으로 변경
    
    * fix: 게시판 Id, 직무이름, 레벨 추가 및 CursorResponseDto로 변경
    
    * fix: 게시판 Id, 직무이름, 레벨 추가
    
    * fix: where절에서 parentCommentId -> boardId 동일한 것으로 변경
    
    * fix: 삭제된 댓글은 댓글 수 카운팅에서 제외
    
    * fix: softDelete 적용 및 CursorResponseDto로 변경
    
    * fix: 전체 댓글 수 반환을 위한 CursorResponseDto에 totalElements 추가
    
    * fix: 부모 댓글 리스트 중 마지막 부모 댓글로 nextCursor 변경
    
    * fix: job fetch join 진행
    
    * fix: 약점 KPI 카드가 없는 예외 제거
    
    * fix: level 컬럼명 'level_value'로 변경
    
    * fix: MessageStrategy 메서드에 user 인자 추가
    
    * fix: 변경된 인자에 맞게 수정
    
    * feat: userIds get 메서드 작성
    
    * feat: Recruitment 엔티티 Notifiable 상속
    
    * feat: 유저에게 fit한 공고 단건 조회 쿼리 작성
    
    * feat: Recruitment에 대한 메시지 전략 작성
    
    * feat: 스케쥴러에서 사용할 알림에 대한 Facade 작성
    
    * feat: 모든 유저에 대한 알림 처리 스케쥴러 작성
    
    * refactor: 유저 fit 공고 조회 시 중복 코드 통일
    
    * chore: 불필요 주석 제거 및 추가
    
    * fix: 코드래빗 리뷰 반영
    
    * feat: 성장 로그 비동기 처리를 위한 PROCESSING 상태 추가
    
    * feat: 성장 로그 평가 대기(PENDING) 상태 저장 메서드 추가
    
    * refactor: 성장 로그 평가 처리를 전략 인터페이스로 분리
    
    * refactor: 동기 성장 로그 평가 로직을 Sync 전략으로 이동
    
    * refactor: GrowthLogEvaluationService를 facade로 전환
    
    * fix: pathvariable 및 네이밍 수정
    
    * feat: 비동기 성장 로그 평가 전략 추가
    
    * chore: 비동기 모드용 Fake publisher 구현 추가
    
    * chore: 성장 로그 평가 모드 설정 추가
    
    * test: 동기 성장 로그 평가 시나리오 테스트를 Sync 전략 테스트로 이동
    
    * test: GrowthLogEvaluationService 테스트를 전략 위임 검증으로 단순화
    
    * chore: 성장 로그 평가 전략을 패키지로 분리
    
    * fix: 코드래빗 반영
    
    * feat: 알림용 추천 공고 저장용 엔티티 생성
    
    * feat: Redis Stream 기반 성장 로그 평가 메시지 발행 구현
    
    * fix: 추천 공고 생성과 발행 분리
    
    * feat: 비동기 enqueue 실패 시 성장 로그 상태를 FAILED로 복구
    
    * feat: 새벽 4시 추천 공고 생성 스케쥴러 생성
    
    * refactor: 성장 로그 평가 로직(buildContext/evaluate)을 코어 서비스로 분리
    
    * fix: 생성과 발행 스케쥴러 분리 및 네이밍 변경
    
    * chore: 불필요 문자 제거
    
    * feat: 비동기 워커용 성장 로그 완료 처리 메서드 추가
    
    - 성장 로그 평가 결과 반영 로직을 공통 메서드로 정리
    - 실패 성장 로그 저장 시 content null/blank 방어
    
    * chore: 성장 로그 워커 폴링 주기 설정 추가
    
    * fix: 코드래빗 반영
    
    * chore: 주석 추가
    
    * fix: 성장로그 평가 publisher DI 충돌 해결
    
    * fix: objectRecord<string, string>으로 수신하도록 변경
    
    * chore: todo 리팩토링 주석 작성
    
    * chore: 불필요 메서드 제거
    
    * fix: 코드래빗 반영
    
    * fix: 텍스트 블록으로 변경
    
    * feat: 전체 댓글 개수 DTO 생성
    
    * feat: 전체 댓글 수 조회와 댓글 목록 조회하는 API 분리하기 위한 레포지터리 생성
    
    * refactor: page -> slice로 구조 변경 및 파라미터 최적화
    
    * fix: totalElements 제거
    
    * feat: 전체 댓글 수 조회 API 구현
    
    * fix: 부모 댓글 단위 페이징으로 변경
    
    * fix: 댓글 조회 조건에 맞는 부모 댓글 단위 페이징으로 변경
    
    * fix: 부모 댓글 단위 페이징 값 (rootComments.size())로 변경
    
    * feat: Redis Stream 기반 성장 로그 평가 worker 추가
    
    * feat: 성장 로그 비동기 평가 processor 분리 및 1건 트랜잭션 처리
    
    * fix: remove trash column
    
    * feat: growth_log에 컬럼 추가
    
    * feat: 성장 로그 평가 메시지에 processingToken 포함
    
    * feat: 성장 로그 상태 전이를 위한 token 기반 repository 메서드 추가
    
    * refactor: 비동기 성장 로그 평가 시 processingToken 발급 및 stream 발행 반영
    
    * feat: feat: processingToken 검증 기반 성장 로그 평가 결과 반영 서비스 구현
    
    * feat: 성장 로그 평가 결과 반영을 위한 internal API 추가
    
    * refactor: 성장 로그 평가 redis worker 활성화 조건 분리
    
    * fix: 타입 불일치 오류 수정
    
    * fix: 함수명 변경 롤백
    
    * refactor: 처리 토큰 멱등 처리 개선
    
    * fix: processingToken을 Stream에 포함
    
    * refactor: 비동기 평가 enqueue 상태머신 및 보상 로직 정리
    
    * feat: 개발환경 토큰 발급 api
    
    * fix: 만료시간 초단위로 관리
    
    * test: SyncGrowthLogEvaluationStrategy 단위 테스트 리팩토링
    
    - GrowthLogEvaluationCoreService 도입에 따른 테스트 구조 변경
    - core 서비스 mock으로 전환하여 의존성 단순화
    - create/retry 성공/실패 케이스 테스트 커버리지 유지
    
    * refactor: 토큰검증 메소드 void 변경
    
    * fix: 임베딩 제거
    
    * refactor: 코드 포매팅
    
    * refactor: 코드래빗 리뷰 반영
    
    * refactor: 포매팅
    
    * chore: 패키지 위치 변경
    
    * test: AsyncGrowthLogEvaluationStrategy 테스트 추가
    