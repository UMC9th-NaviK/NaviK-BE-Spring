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
import navik.domain.kpi.service.command.KpiScoreIncrementService;
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
	private final KpiScoreIncrementService kpiScoreIncrementService;

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

		Long id = growthLogRepository.save(growthLog).getId();

		applyKpiScoreDeltas(userId, kpis);

		return id;
	}

	public Long savePendingUserInputLog(Long userId, String content) {
		User user = userRepository.getReferenceById(userId);

		GrowthLog growthLog = newUserInputLog(
			user,
			"평가 중입니다.",          // placeholder title
			safe(content),            // 원본 입력 저장 (null/blank 방어)
			0,                        // totalDelta는 평가 전이므로 0
			GrowthLogStatus.PENDING   // 비동기 평가 대기
		);

		return growthLogRepository.save(growthLog).getId();
	}

	private String safe(String s) {
		return (s == null || s.isBlank()) ? "(내용 없음)" : s.trim();
	}

	public Long saveFailedUserInputLog(Long userId, String content) {
		User user = userRepository.getReferenceById(userId);

		GrowthLog growthLog = newUserInputLog(
			user,
			"제목이 아직 정해지지 않았습니다.",
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
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));

		// 선점된 PENDING만 반영 가능
		if (growthLog.getStatus() != GrowthLogStatus.PENDING) {
			throw new GeneralExceptionHandler(GrowthLogErrorCode.INVALID_GROWTH_LOG_STATUS);
		}

		growthLog.clearKpiLinks();
		growthLog.applyEvaluation(normalized.title(), normalized.content(), totalDelta);

		attachKpiLinks(growthLog, kpis);
		applyKpiScoreDeltas(userId, kpis);
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

	private void applyKpiScoreDeltas(
		Long userId,
		List<GrowthLogEvaluationResult.KpiDelta> kpis
	) {
		for (var kd : kpis) {
			kpiScoreIncrementService.incrementInternal(
				userId,
				kd.kpiCardId(),
				kd.delta()
			);
		}
	}
}
