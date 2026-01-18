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
import navik.domain.growthLog.exception.code.GrowthLogErrorCode;
import navik.domain.growthLog.repository.GrowthLogRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

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

	@Transactional(readOnly = true)
	public GrowthLog getDetail(Long userId, Long growthLogId) {
		return growthLogRepository.findDetailByIdAndUserId(growthLogId, userId)
			.orElseThrow(() -> new GeneralExceptionHandler(GrowthLogErrorCode.GROWTH_LOG_NOT_FOUND));
	}
}
