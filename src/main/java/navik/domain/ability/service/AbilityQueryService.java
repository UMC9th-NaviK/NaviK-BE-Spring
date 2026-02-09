package navik.domain.ability.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.converter.AbilityConverter;
import navik.domain.ability.dto.AbilityRequestDTO;
import navik.domain.ability.dto.AbilityResponseDTO;
import navik.domain.ability.entity.Ability;
import navik.domain.ability.repository.AbilityRepository;
import navik.domain.users.entity.User;
import navik.domain.users.repository.UserRepository;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.dto.CursorResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbilityQueryService {

	private final UserRepository userRepository;
	private final AbilityRepository abilityRepository;

	public CursorResponseDTO<AbilityResponseDTO.AbilityDTO> getAbilities(Long userId, String cursor, int size) {

		// 1. 유저 검색
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));

		if (size <= 0 || size > 100) {
			throw new GeneralException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		// 2. 커서 디코딩
		AbilityRequestDTO.CursorRequest cursorRequest = decodeCursor(cursor);
		LocalDateTime lastCreatedAt = cursorRequest != null ? cursorRequest.getLastCreatedAt() : null;
		Long lastId = cursorRequest != null ? cursorRequest.getLastId() : null;

		Slice<Ability> abilitySlice;
		PageRequest pageRequest = PageRequest.of(0, size);

		// 3. 커서 유무에 따른 분기 처리
		if (cursorRequest != null && lastCreatedAt != null && lastId != null) {
			abilitySlice = abilityRepository.findByUserWithCursor(user, lastCreatedAt, lastId, pageRequest);
		} else {
			abilitySlice = abilityRepository.findByUserOrderByCreatedAtDescIdDesc(user, pageRequest);
		}

		List<AbilityResponseDTO.AbilityDTO> abilityDTOs = abilitySlice.getContent().stream()
			.map(AbilityConverter::toAbilityDTO)
			.toList();

		String nextCursor = null;
		if (abilitySlice.hasNext() && !abilityDTOs.isEmpty()) {
			Ability lastAbility = abilitySlice.getContent().get(abilitySlice.getContent().size() - 1);
			nextCursor = encodeCursor(lastAbility.getCreatedAt(), lastAbility.getId());
		}

		return CursorResponseDTO.of(abilityDTOs, abilitySlice.hasNext(), nextCursor);
	}

	private String encodeCursor(LocalDateTime createdAt, Long id) {
		String original = createdAt + "_" + id;
		return Base64.getEncoder().encodeToString(original.getBytes());
	}

	private AbilityRequestDTO.CursorRequest decodeCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}

		try {
			String decoded = new String(Base64.getDecoder().decode(cursor));

			String[] parts = decoded.split("_");
			if (parts.length != 2) {
				throw new GeneralException(GeneralErrorCode.INVALID_INPUT_VALUE);
			}

			LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
			Long id = Long.parseLong(parts[1]);

			return AbilityRequestDTO.CursorRequest.builder()
				.lastCreatedAt(createdAt)
				.lastId(id)
				.build();

		} catch (Exception e) {
			// 파싱 실패(DateTimeParseException, NumberFormatException 등) 시 예외 처리
			throw new GeneralException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}
	}
}
