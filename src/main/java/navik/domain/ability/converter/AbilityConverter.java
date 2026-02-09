package navik.domain.ability.converter;

import navik.domain.ability.dto.AbilityResponseDTO;
import navik.domain.ability.entity.Ability;

public class AbilityConverter {
    public static AbilityResponseDTO.AbilityDTO toAbilityDTO(Ability ability) {
        return AbilityResponseDTO.AbilityDTO.builder()
                .abilityId(ability.getId())
                .content(ability.getContent())
                .build();
    }
}
