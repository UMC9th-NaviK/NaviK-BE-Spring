package navik.domain.level.policy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import navik.domain.level.entity.Level;
import navik.domain.recruitment.enums.JobType;

@Component
public class LevelDescriptionPolicyRegistry {

	private final Map<JobType, LevelDescriptionPolicy> policies;

	public LevelDescriptionPolicyRegistry(List<LevelDescriptionPolicy> beans) {
		this.policies = new EnumMap<>(JobType.class);
		for (LevelDescriptionPolicy p : beans) {
			this.policies.put(p.supports(), p);
		}
	}

	public String getDescription(JobType jobType, Level level) {
		LevelDescriptionPolicy policy = policies.get(jobType);
		if (policy == null) {
			return "레벨 설명이 준비되지 않았습니다.";
		}
		return policy.getDescription(level);
	}
}
