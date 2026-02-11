package navik.domain.level.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.domain.level.dto.LevelResponseDTO;
import navik.domain.level.entity.Level;
import navik.domain.level.policy.LevelDescriptionPolicyRegistry;
import navik.domain.recruitment.enums.JobType;
import navik.domain.recruitment.exception.code.RecruitmentErrorCode;
import navik.domain.users.entity.User;
import navik.global.apiPayload.exception.exception.GeneralException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelQueryService {

	private final LevelDescriptionPolicyRegistry descriptionRegistry;

	public int calculateLevel(Long totalScore) {
		return Level.fromScore(totalScore).getValue();
	}

	public LevelResponseDTO.LevelResult getLevelInfo(User user, Long totalScore) {

		JobType jobType = Optional
			.ofNullable(JobType.getByLabel(user.getJob().getName()))
			.orElseThrow(() -> new GeneralException(RecruitmentErrorCode.JOB_TYPE_NOT_FOUND));

		Level level = Level.fromScore(totalScore);
		int percentage = calculatePercentage(totalScore, level);

		String description = descriptionRegistry.getDescription(jobType, level);

		return new LevelResponseDTO.LevelResult(level.getValue(), description, percentage);
	}

	// 총 점수로 레벨 퍼센트 반환
	public int calculatePercentage(Long totalScore, Level current) {

		if (totalScore == null || totalScore <= 0)
			return 0;

		Level[] levels = Level.values();
		if (current.ordinal() == levels.length - 1)
			return 100;

		Level next = levels[current.ordinal() + 1];

		long currentMin = current.getMinScore();
		long nextMin = next.getMinScore();

		if (nextMin <= currentMin) {
			log.warn("Invalid level range. current={}, currentMin={}, nextMin={}", current, currentMin, nextMin);
			return 0;
		}

		double progress = (double)(totalScore - currentMin) / (nextMin - currentMin);
		return Math.min(100, Math.max(0, (int)(progress * 100)));
	}

}
