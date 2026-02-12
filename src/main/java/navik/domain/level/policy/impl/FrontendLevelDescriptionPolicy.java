package navik.domain.level.policy.impl;

import org.springframework.stereotype.Component;

import navik.domain.level.entity.Level;
import navik.domain.level.policy.LevelDescriptionPolicy;
import navik.domain.recruitment.enums.JobType;

@Component
public class FrontendLevelDescriptionPolicy implements LevelDescriptionPolicy {

	@Override
	public JobType supports() {
		return JobType.FRONTEND;
	}

	@Override
	public String getDescription(Level level) {
		return switch (level) {
			case LEVEL_1 -> "웹의 기본 구조를 이해하고 화면 구현을 배우는 단계입니다.";
			case LEVEL_2 -> "주어진 디자인을 바탕으로 화면을 구현할 수 있습니다. 기존 컴포넌트를 활용할 수 있습니다.";
			case LEVEL_3 -> "컴포넌트 단위로 화면을 구성하고 상태 흐름을 연결할 수 있습니다.";
			case LEVEL_4 -> "재사용성과 구조를 고려해 화면 단위 기능을 설계하고 개선할 수 있습니다.";
			case LEVEL_5 -> "사용자 경험과 성능을 함께 고민하며 UI 구조를 설계합니다. 구현 이유를 설명할 수 있습니다.";
			case LEVEL_6 -> "프로젝트 단위 프론트엔드 개발을 주도합니다. 기술 선택과 구조 개선에 의견을 제시합니다.";
			case LEVEL_7 -> "서비스 전반의 화면 흐름과 상태 구조를 설계합니다.";
			case LEVEL_8 -> "성능 이슈를 분석하고 개선 성과를 만듭니다. 팀의 구현 기준에 영향을 줍니다.";
			case LEVEL_9 -> "프론트엔드 아키텍처와 협업 구조를 설계합니다.";
			case LEVEL_10 -> "기술 방향성과 구현 기준을 만들어내는 단계입니다. 서비스 내 상위 0.1% 수준입니다.";
		};
	}
}
