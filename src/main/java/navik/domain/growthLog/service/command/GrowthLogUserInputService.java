package navik.domain.growthLog.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.domain.kpi.repository.KpiCardRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class GrowthLogUserInputService {

	private final GrowthLogRepository growthLogRepository;
	private final KpiCardRepository kpiCardRepository;

	public Long create(GrowthLogRequestDTO.CreateUserInput req) {
		GrowthLog growthLog = GrowthLog.builder()
			.kpiCard(null)
			.type(GrowthType.USER_INPUT)
			.title(req.title().trim())
			.content(req.content().trim())
			.score(0)
			.build();

		return growthLogRepository.save(growthLog).getId();
	}

}