package navik.domain.level.policy.impl;

import org.springframework.stereotype.Component;

import navik.domain.level.entity.Level;
import navik.domain.level.policy.LevelDescriptionPolicy;
import navik.domain.recruitment.enums.JobType;

@Component
public class BackendLevelDescriptionPolicy implements LevelDescriptionPolicy {

	@Override
	public JobType supports() {
		return JobType.BACKEND;
	}

	@Override
	public String getDescription(Level level) {
		return switch (level) {
			case LEVEL_1 -> "서버와 API, DB가 어떻게 연결되는지 배우는 단계입니다. 백엔드 전체의 흐름을 이해하기 시작합니다.";
			case LEVEL_2 -> "CRUD 중심의 기능을 구현해본 경험이 있습니다. 주어진 구조 안에서 서버 기능을 개발할 수 있습니다.";
			case LEVEL_3 -> "API와 DB 흐름을 이해하고 기본 기능을 단독으로 구현할 수 있습니다. 데이터 모델링을 적용하기 시작합니다.";
			case LEVEL_4 -> "요구사항에 맞는 서버 구조를 설계할 수 있습니다. 예외 처리와 데이터 흐름을 고려해 구현합니다.";
			case LEVEL_5 -> "성능·보안·확장성을 고민하며 설계를 설명할 수 있습니다. 단순 구현이 아닌 구조적 선택을 합니다.";
			case LEVEL_6 -> "프로젝트 단위의 백엔드 개발을 주도한 경험이 있습니다. 배포와 운영 환경까지 고려합니다.";
			case LEVEL_7 -> "서비스 전체 백엔드 구조를 설계할 수 있습니다. 모듈과 책임 구조를 명확히 정의합니다.";
			case LEVEL_8 -> "장애 대응과 성능 개선 경험을 바탕으로 팀의 신뢰를 얻습니다. 기술 판단에 영향을 줍니다.";
			case LEVEL_9 -> "중·장기 확장성을 고려한 시스템 구조를 설계합니다. 기술적 방향성을 제시합니다.";
			case LEVEL_10 -> "백엔드 시스템의 기준을 만드는 단계입니다. 서비스 내 상위 0.1% 수준입니다.";
		};
	}
}
