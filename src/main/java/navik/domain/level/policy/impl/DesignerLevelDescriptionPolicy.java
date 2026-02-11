package navik.domain.level.policy.impl;

import org.springframework.stereotype.Component;

import navik.domain.level.entity.Level;
import navik.domain.level.policy.LevelDescriptionPolicy;
import navik.domain.recruitment.enums.JobType;

@Component
public class DesignerLevelDescriptionPolicy implements LevelDescriptionPolicy {

	@Override
	public JobType supports() {
		return JobType.DESIGNER;
	}

	@Override
	public String getDescription(Level level) {
		return switch (level) {
			case LEVEL_1 -> "디자인의 목적과 사용자 흐름을 이해하는 단계입니다.";
			case LEVEL_2 -> "주어진 요구사항에 맞춰 화면을 설계하고 수정할 수 있습니다.";
			case LEVEL_3 -> "사용자 흐름을 고려해 와이어프레임과 시안을 제작합니다.";
			case LEVEL_4 -> "문제를 발견하고 디자인으로 해결할 수 있습니다.";
			case LEVEL_5 -> "사용성과 일관성을 고려해 디자인 판단을 내리고 설명할 수 있습니다.";
			case LEVEL_6 -> "프로젝트 단위 디자인을 주도하며 협업을 이끕니다.";
			case LEVEL_7 -> "서비스 전반의 UX 흐름을 설계하고 개선합니다.";
			case LEVEL_8 -> "사용자 반응과 지표를 기반으로 성과를 만들어냅니다.";
			case LEVEL_9 -> "디자인 시스템과 브랜드 방향성을 설계합니다.";
			case LEVEL_10 -> "디자인 기준과 사용자 경험의 방향을 만드는 단계입니다. 서비스 내 상위 0.1% 수준입니다.";
		};
	}
}
