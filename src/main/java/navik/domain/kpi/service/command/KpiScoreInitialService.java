package navik.domain.kpi.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.internal.GrowthLogInternalCreateRequest;
import navik.domain.growthLog.service.command.GrowthLogInternalService;
import navik.domain.kpi.dto.internal.KpiScoreInitializeResult;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.entity.KpiScore;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.kpi.exception.code.KpiScoreErrorCode;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

@Service
@RequiredArgsConstructor
@Transactional
public class KpiScoreInitialService {

	private final KpiScoreRepository kpiScoreRepository;
	private final KpiCardRepository kpiCardRepository;
	private final UserRepository userRepository;

	private final GrowthLogInternalService growthLogInternalService;

	public KpiScoreResponseDTO.Initialize initializeKpiScores(
		Long userId,
		KpiScoreRequestDTO.Initialize request
	) {
		List<KpiScoreRequestDTO.Item> items = request.scores();

		List<Long> cardIds = extractCardIds(items);
		validateNoDuplicateCardIds(cardIds);

		User userRef = userRepository.getReferenceById(userId);

		Map<Long, KpiCard> cardMap = loadCardMapOrThrow(cardIds);
		Map<Long, KpiScore> existingMap = loadExistingScoreMap(userId, cardIds);

		KpiScoreInitializeResult result = upsertScores(userRef, items, cardMap, existingMap);

		if (!result.toCreate().isEmpty()) {
			kpiScoreRepository.saveAll(result.toCreate());
		}

		createPortfolioGrowthLogIfNeeded(userId, result.kpiDeltasForLog());

		return new KpiScoreResponseDTO.Initialize(
			result.created(),
			result.updated(),
			result.resultItems()
		);
	}

	private List<Long> extractCardIds(List<KpiScoreRequestDTO.Item> items) {
		return items.stream()
			.map(KpiScoreRequestDTO.Item::kpiCardId)
			.toList();
	}

	private void validateNoDuplicateCardIds(List<Long> cardIds) {
		if (cardIds.stream().distinct().count() != cardIds.size()) {
			throw new GeneralExceptionHandler(KpiScoreErrorCode.DUPLICATED_KPI_CARD_ID);
		}
	}

	private Map<Long, KpiCard> loadCardMapOrThrow(List<Long> cardIds) {
		List<KpiCard> cards = kpiCardRepository.findAllById(cardIds);

		if (cards.size() != cardIds.stream().distinct().count()) {
			throw new GeneralExceptionHandler(KpiCardErrorCode.KPI_CARD_NOT_FOUND);
		}

		return cards.stream()
			.collect(Collectors.toMap(
				KpiCard::getId,
				c -> c
			));
	}

	private Map<Long, KpiScore> loadExistingScoreMap(Long userId, List<Long> cardIds) {
		return kpiScoreRepository.findAllByUserIdAndKpiCard_IdIn(userId, cardIds).stream()
			.collect(Collectors.toMap(
				s -> s.getKpiCard().getId(),
				s -> s
			));
	}

	private KpiScoreInitializeResult upsertScores(
		User userRef,
		List<KpiScoreRequestDTO.Item> items,
		Map<Long, KpiCard> cardMap,
		Map<Long, KpiScore> existingMap
	) {
		int created = 0;
		int updated = 0;

		List<KpiScore> toCreate = new ArrayList<>();
		List<KpiScoreResponseDTO.Item> resultItems = new ArrayList<>();
		List<GrowthLogInternalCreateRequest.KpiDelta> kpiDeltasForLog = new ArrayList<>();

		for (KpiScoreRequestDTO.Item item : items) {
			Long kpiCardId = item.kpiCardId();
			Integer score = item.score();

			KpiScore existing = existingMap.get(kpiCardId);

			if (existing != null) {
				existing.updateScore(score);
				updated++;
				resultItems.add(new KpiScoreResponseDTO.Item(kpiCardId, score, false));
			} else {
				KpiScore newScore = KpiScore.builder()
					.user(userRef)
					.kpiCard(cardMap.get(kpiCardId))
					.score(score)
					.build();

				toCreate.add(newScore);
				created++;
				resultItems.add(new KpiScoreResponseDTO.Item(kpiCardId, score, true));
			}

			// delta = score, score==0은 기록 안 함
			if (score != 0) {
				kpiDeltasForLog.add(new GrowthLogInternalCreateRequest.KpiDelta(kpiCardId, score));
			}
		}

		return new KpiScoreInitializeResult(created, updated, toCreate, resultItems, kpiDeltasForLog);
	}

	private void createPortfolioGrowthLogIfNeeded(Long userId, List<GrowthLogInternalCreateRequest.KpiDelta> deltas) {
		if (deltas.isEmpty()) {
			return;
		}
		growthLogInternalService.createPortfolio(userId, new GrowthLogInternalCreateRequest(deltas, null));
	}
}
