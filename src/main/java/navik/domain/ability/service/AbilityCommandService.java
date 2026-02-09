package navik.domain.ability.service;

import static navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.entity.Ability;
import navik.domain.ability.entity.AbilityEmbedding;
import navik.domain.ability.repository.AbilityEmbeddingRepository;
import navik.domain.ability.repository.AbilityRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AbilityCommandService {

	private final AbilityRepository abilityRepository;
	private final AbilityEmbeddingRepository abilityEmbeddingRepository;
	private final UserRepository userRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveAbilities(Long userId,
		List<GrowthLogEvaluationResult.AbilityResult> abilities) {
		if (abilities == null || abilities.isEmpty()) {
			return;
		}

		User user = userRepository.getReferenceById(userId);

		for (GrowthLogEvaluationResult.AbilityResult abilityResult : abilities) {
			Ability ability = Ability.builder()
				.content(abilityResult.content())
				.user(user)
				.build();

			Ability savedAbility = abilityRepository.save(ability);

			AbilityEmbedding embedding = AbilityEmbedding.builder()
				.ability(savedAbility)
				.embedding(abilityResult.embedding())
				.build();

			abilityEmbeddingRepository.save(embedding);
		}
	}
}
