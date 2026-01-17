package navik.domain.growthLog.service.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.entity.GrowthLogKpiLink;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class GrowthLogPersistenceService {

	private final UserRepository userRepository;
	private final GrowthLogRepository growthLogRepository;
	private final KpiCardRepository kpiCardRepository;

	public Long persist(
		Long userId,
		GrowthLogEvaluationResult normalized,
		int totalDelta,
		List<GrowthLogEvaluationResult.KpiDelta> kpis
	) {
		User user = userRepository.getReferenceById(userId);

		GrowthLog growthLog = GrowthLog.builder()
			.user(user)
			.type(GrowthType.USER_INPUT)
			.title(normalized.title())
			.content(normalized.content())
			.totalDelta(totalDelta)
			.build();

		for (var kd : kpis) {
			KpiCard ref = kpiCardRepository.getReferenceById(kd.kpiCardId());
			growthLog.addKpiLink(GrowthLogKpiLink.builder()
				.kpiCard(ref)
				.delta(kd.delta())
				.build());
		}

		return growthLogRepository.save(growthLog).getId();
	}
}
