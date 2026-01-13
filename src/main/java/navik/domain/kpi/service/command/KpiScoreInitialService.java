package navik.domain.kpi.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import navik.domain.kpi.dto.req.KpiScoreRequestDTO;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.entity.KpiCard;
import navik.domain.kpi.entity.KpiScore;
import navik.domain.kpi.exception.code.KpiErrorCode;
import navik.domain.kpi.repository.KpiCardRepository;
import navik.domain.kpi.repository.KpiScoreRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KpiScoreInitialService {

    private final KpiScoreRepository kpiScoreRepository;
    private final KpiCardRepository kpiCardRepository;
    private final UserRepository userRepository;

    public KpiScoreResponseDTO.Initialize initializeKpiScores(
            Long userId,
            KpiScoreRequestDTO.Initialize request
    ) {
        List<KpiScoreRequestDTO.Item> items = request.scores();
        validateItems(items);

        List<Long> cardIds = items.stream()
                .map(KpiScoreRequestDTO.Item::kpiCardId)
                .toList();

        // 중복 검증
        if (cardIds.stream().distinct().count() != cardIds.size()) {
            throw new GeneralExceptionHandler(KpiErrorCode.DUPLICATED_KPI_CARD_ID);
        }

        User userRef = userRepository.getReferenceById(userId);

        Map<Long, KpiCard> cardMap = kpiCardRepository.findAllById(cardIds).stream()
                .collect(Collectors.toMap(KpiCard::getId, Function.identity()));

        List<Long> missing = cardIds.stream()
                .distinct()
                .filter(id -> !cardMap.containsKey(id))
                .toList();

        if (!missing.isEmpty()) {
            throw new GeneralExceptionHandler(KpiErrorCode.KPI_CARD_NOT_FOUND);
        }

        // 기존 점수 조회
        Map<Long, KpiScore> existingMap =
                kpiScoreRepository.findAllByUserIdAndKpiCard_IdIn(userId, cardIds).stream()
                        .collect(Collectors.toMap(
                                s -> s.getKpiCard().getId(),
                                Function.identity()
                        ));

        // 초기화 로직 (create / update)
        int created = 0;
        int updated = 0;

        List<KpiScore> toCreate = new ArrayList<>();
        List<KpiScoreResponseDTO.Item> resultItems = new ArrayList<>();

        for (KpiScoreRequestDTO.Item item : items) {
            Long kpiCardId = item.kpiCardId();
            Integer score = item.score();

            KpiScore existing = existingMap.get(kpiCardId);

            if (existing != null) {
                existing.updateScore(score);
                updated++;
                resultItems.add(new KpiScoreResponseDTO.Item(kpiCardId, score, false));
                continue;
            }

            KpiScore newScore = KpiScore.builder()
                    .user(userRef)
                    .kpiCard(cardMap.get(kpiCardId))
                    .score(score)
                    .build();

            toCreate.add(newScore);
            created++;
            resultItems.add(new KpiScoreResponseDTO.Item(kpiCardId, score, true));
        }

        if (!toCreate.isEmpty()) {
            kpiScoreRepository.saveAll(toCreate);
        }

        return new KpiScoreResponseDTO.Initialize(
                created,
                updated,
                resultItems
        );
    }

    private void validateItems(List<KpiScoreRequestDTO.Item> items) {
        if (items == null || items.isEmpty()) {
            throw new GeneralExceptionHandler(KpiErrorCode.EMPTY_KPI_SCORES);
        }

        for (KpiScoreRequestDTO.Item item : items) {
            if (item.kpiCardId() == null || item.score() == null) {
                throw new GeneralExceptionHandler(KpiErrorCode.INVALID_KPI_SCORE_REQUEST);
            }
        }
    }

}
