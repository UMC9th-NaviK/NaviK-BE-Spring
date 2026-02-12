package navik.domain.ability.normalizer;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;
import navik.domain.portfolio.dto.PortfolioAiDTO;

@Component
@Slf4j
public class AbilityNormalizer {

	private static final int EMBEDDING_DIM = 1536;
	private static final int CONTENT_LOG_MAX = 30;

	public List<GrowthLogAiResponseDTO.GrowthLogEvaluationResult.AbilityResult> normalize(
		List<GrowthLogAiResponseDTO.GrowthLogEvaluationResult.AbilityResult> abilities
	) {
		if (abilities == null || abilities.isEmpty()) {
			return List.of();
		}

		return abilities.stream()
			.filter(a -> {
				if (a == null) {
					log.warn("Invalid ability filtered: null");
					return false;
				}

				String content = a.content();
				if (content == null || content.isBlank()) {
					log.warn("Invalid ability content filtered: blank");
					return false;
				}

				float[] embedding = a.embedding();
				if (embedding == null || embedding.length != EMBEDDING_DIM) {
					log.warn("Invalid ability embedding dimension filtered: content={}, dimension={}",
						abbreviate(content.trim()),
						embedding == null ? "null" : embedding.length
					);
					return false;
				}

				for (float v : embedding) {
					if (!Float.isFinite(v)) {
						log.warn("Invalid ability embedding value filtered: content={}",
							abbreviate(content.trim()));
						return false;
					}
				}

				return true;
			})
			.map(a -> new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.AbilityResult(
				a.content().trim(),
				a.embedding()
			))
			.toList();
	}

	public List<PortfolioAiDTO.Abilities> normalizeFromPortfolio(List<PortfolioAiDTO.Abilities> abilities) {
		if (abilities == null || abilities.isEmpty()) {
			return List.of();
		}

		return abilities.stream()
			.filter(a -> {
				if (a == null) {
					log.warn("Invalid ability filtered: null");
					return false;
				}

				String content = a.content();
				if (content == null || content.isBlank()) {
					log.warn("Invalid ability content filtered: blank");
					return false;
				}

				float[] embedding = a.embedding();
				if (embedding == null || embedding.length != EMBEDDING_DIM) {
					log.warn("Invalid ability embedding dimension filtered: content={}, dimension={}",
						abbreviate(content.trim()),
						embedding == null ? "null" : embedding.length
					);
					return false;
				}

				for (float v : embedding) {
					if (!Float.isFinite(v)) {
						log.warn("Invalid ability embedding value filtered: content={}",
							abbreviate(content.trim()));
						return false;
					}
				}

				return true;
			})
			.map(a -> new PortfolioAiDTO.Abilities(a.content().trim(), a.embedding()))
			.toList();
	}

	private String abbreviate(String s) {
		return (s.length() <= CONTENT_LOG_MAX) ? s : s.substring(0, CONTENT_LOG_MAX) + "...";
	}
}
