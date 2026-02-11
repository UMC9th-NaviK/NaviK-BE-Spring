package navik.domain.level.policy.impl;

import org.springframework.stereotype.Component;

import navik.domain.level.entity.Level;
import navik.domain.level.policy.LevelDescriptionPolicy;
import navik.domain.recruitment.enums.JobType;

@Component
public class PmLevelDescriptionPolicy implements LevelDescriptionPolicy {

	@Override
	public JobType supports() {
		return JobType.PM;
	}

	@Override
	public String getDescription(Level level) {
		return switch (level) {
			case LEVEL_1 -> "서비스 흐름을 이해하고 요구사항의 맥락을 배우는 단계입니다.";
			case LEVEL_2 -> "요구사항을 문서로 정리하고 기능을 구조화할 수 있습니다.";
			case LEVEL_3 -> "기능을 사용자 흐름 기준으로 정의하고 협업을 고려해 기획합니다.";
			case LEVEL_4 -> "문제 정의부터 기능 설계까지 스스로 수행할 수 있습니다.";
			case LEVEL_5 -> "데이터와 사용자 반응을 기반으로 의사결정을 내립니다. 왜 필요한지 명확히 설명할 수 있습니다.";
			case LEVEL_6 -> "프로젝트 단위 기획을 주도하며 일정·리스크를 조정합니다.";
			case LEVEL_7 -> "서비스 흐름 전반을 설계하고 실험–개선 사이클을 운영합니다.";
			case LEVEL_8 -> "핵심 지표를 설정하고 팀 단위 성과 개선을 이끕니다.";
			case LEVEL_9 -> "서비스의 중·장기 방향성을 설계하고 이해관계를 조율합니다.";
			case LEVEL_10 -> "기획 기준과 의사결정 문화를 만드는 단계입니다. 서비스 내 상위 0.1% 수준입니다.";
		};
	}
}
