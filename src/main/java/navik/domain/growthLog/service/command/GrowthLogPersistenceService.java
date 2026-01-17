package navik.domain.growthLog.service.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.entity.GrowthLogKpiLink;
import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional
public class GrowthLogPersistenceService {

	private final UserRepository userRepository;
	private final GrowthLogRepository growthLogRepository;
	private final KpiCardRepository kpiCardRepository;

	public Long saveUserInputLog(
		Long userId,
		GrowthLogEvaluationResult normalized,
		int totalDelta,
		List<GrowthLogEvaluationResult.KpiDelta> kpis
	) {
		User user = userRepository.getReferenceById(userId);

		GrowthLog growthLog = newUserInputLog(
			user,
			normalized.title(),
			normalized.content(),
			totalDelta,
			GrowthLogStatus.COMPLETED
		);

		attachKpiLinks(growthLog, kpis);

		return growthLogRepository.save(growthLog).getId();
	}

	public Long saveFailedUserInputLog(Long userId, String title, String content) {
		User user = userRepository.getReferenceById(userId);

		GrowthLog growthLog = newUserInputLog(
			user,
			title,
			content,
			0,
			GrowthLogStatus.FAILED
		);

		return growthLogRepository.save(growthLog).getId();
	}

	public void updateGrowthLogAfterRetry(
		Long userId,
		Long growthLogId,
		GrowthLogEvaluationResult normalized,
		int totalDelta,
		List<GrowthLogEvaluationResult.KpiDelta> kpis
	) {
		GrowthLog growthLog = growthLogRepository.findByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralExceptionHandler(
				GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND
			));

		growthLog.clearKpiLinks();
		growthLog.applyEvaluation(normalized.title(), normalized.content(), totalDelta);

		attachKpiLinks(growthLog, kpis);
	}

	private GrowthLog newUserInputLog(
		User user,
		String title,
		String content,
		int totalDelta,
		GrowthLogStatus status
	) {
		return GrowthLog.builder()
			.user(user)
			.type(GrowthType.USER_INPUT)
			.title(title)
			.content(content)
			.totalDelta(totalDelta)
			.status(status)
			.build();
	}

	private void attachKpiLinks(GrowthLog growthLog, List<GrowthLogEvaluationResult.KpiDelta> kpis) {
		for (var kd : kpis) {
			KpiCard ref = kpiCardRepository.getReferenceById(kd.kpiCardId());
			growthLog.addKpiLink(GrowthLogKpiLink.builder()
				.kpiCard(ref)
				.delta(kd.delta())
				.build());
		}
	}
}
