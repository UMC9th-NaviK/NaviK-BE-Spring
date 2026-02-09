package navik.domain.ability.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.ability.dto.AbilityResponseDTO;
import navik.domain.ability.service.AbilityQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequestMapping("/v1/abilities")
@RequiredArgsConstructor
public class AbilityController implements AbilityControllerDocs {

	private final AbilityQueryService abilityQueryService;

	@Override
	@GetMapping
	public ApiResponse<CursorResponseDTO<AbilityResponseDTO.AbilityDTO>> getAbilities(
		@AuthUser Long userId,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CursorResponseDTO<AbilityResponseDTO.AbilityDTO> result = abilityQueryService.getAbilities(userId, cursor,
			size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}
}
