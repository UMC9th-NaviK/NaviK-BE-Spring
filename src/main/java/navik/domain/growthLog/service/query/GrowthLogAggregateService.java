package navik.domain.growthLog.service.query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.enums.AggregateUnit;
import navik.domain.growthLog.enums.GrowthType;
import navik.domain.growthLog.repository.GrowthLogRepository;

@Service
@RequiredArgsConstructor
public class GrowthLogAggregateService {

	private final GrowthLogRepository growthLogRepository;

	public List<GrowthLogResponseDTO.Point> getScoreTimeline(
		Long userId,
		AggregateUnit unit,
		GrowthType type
	) {
		LocalDateTime first = (type == null)
			? growthLogRepository.findFirstCreatedAt(userId).orElse(null)
			: growthLogRepository.findFirstCreatedAtByType(userId, type).orElse(null);

		if (first == null) {
			return List.of(); // 로그가 없으면 빈 리스트
		}

		LocalDate startDate = first.toLocalDate();
		LocalDate today = LocalDate.now(); // 서버 TZ 기준(한국이면 OK)

		// end는 "다음날 0시"로 (쿼리에서 < end)
		LocalDateTime start = startDate.atStartOfDay();
		LocalDateTime end = today.plusDays(1).atStartOfDay();

		String typeParam = (type == null) ? null : type.name();

		// DB에서 "있는 구간"만 받아오기
		List<Object[]> rows = switch (unit) {
			case DAY -> growthLogRepository.sumByDay(userId, typeParam, start, end);
			case WEEK -> growthLogRepository.sumByWeek(userId, typeParam, start, end);
			case MONTH -> growthLogRepository.sumByMonth(userId, typeParam, start, end);
		};

		// period_start(LocalDate) -> sumScore
		Map<LocalDate, Integer> sumMap = new HashMap<>();
		for (Object[] r : rows) {
			LocalDate periodStart = ((java.sql.Date)r[0]).toLocalDate();
			int sumScore = ((Number)r[1]).intValue();
			sumMap.put(periodStart, sumScore);
		}

		// 빈 구간 0 채우면서 누적 계산
		return switch (unit) {
			case DAY -> fillDaily(sumMap, startDate, today);
			case WEEK -> fillWeekly(sumMap, startDate, today);
			case MONTH -> fillMonthly(sumMap, startDate, today);
		};
	}

	private List<GrowthLogResponseDTO.Point> fillDaily(
		Map<LocalDate, Integer> sumMap,
		LocalDate startDate,
		LocalDate endDate
	) {
		DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
		int cumulative = 0;

		List<GrowthLogResponseDTO.Point> out = new ArrayList<>();
		for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
			int sum = sumMap.getOrDefault(d, 0);
			cumulative += sum;
			out.add(new GrowthLogResponseDTO.Point(fmt.format(d), sum, cumulative));
		}
		return out;
	}

	private List<GrowthLogResponseDTO.Point> fillWeekly(
		Map<LocalDate, Integer> sumMap,
		LocalDate startDate,
		LocalDate endDate
	) {
		DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

		// 주 시작일(월요일)로 정렬
		LocalDate startWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate endWeek = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		int cumulative = 0;
		List<GrowthLogResponseDTO.Point> out = new ArrayList<>();

		for (LocalDate w = startWeek; !w.isAfter(endWeek); w = w.plusWeeks(1)) {
			int sum = sumMap.getOrDefault(w, 0); // w = 주 시작일 키
			cumulative += sum;
			out.add(new GrowthLogResponseDTO.Point(fmt.format(w), sum, cumulative));
		}
		return out;
	}

	private List<GrowthLogResponseDTO.Point> fillMonthly(
		Map<LocalDate, Integer> sumMap,
		LocalDate startDate,
		LocalDate endDate
	) {
		DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("yyyy-MM");

		YearMonth startYm = YearMonth.from(startDate);
		YearMonth endYm = YearMonth.from(endDate);

		int cumulative = 0;
		List<GrowthLogResponseDTO.Point> out = new ArrayList<>();

		for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
			LocalDate monthStart = ym.atDay(1); // date_trunc('month') 키와 동일
			int sum = sumMap.getOrDefault(monthStart, 0);
			cumulative += sum;
			out.add(new GrowthLogResponseDTO.Point(ym.format(ymFmt), sum, cumulative));
		}
		return out;
	}

}
