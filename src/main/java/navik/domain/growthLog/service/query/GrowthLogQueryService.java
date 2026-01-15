package navik.domain.growthLog.service.query;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.entity.GrowthLog;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.repository.GrowthLogRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrowthLogQueryService {

	private final GrowthLogRepository growthLogRepository;

	public Page<GrowthLog> getMonthlyLogs(
		Long userId,
		YearMonth yearMonth,
		GrowthType type,
		Pageable pageable
	) {
		LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
		LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

		if (type == null) {
			return growthLogRepository.findMonthly(
				userId, start, end, pageable
			);
		}

		return growthLogRepository.findMonthlyByType(
			userId, type, start, end, pageable
		);
	}
}
