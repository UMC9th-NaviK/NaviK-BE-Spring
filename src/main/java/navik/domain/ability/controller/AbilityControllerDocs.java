package navik.domain.ability.controller;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.ability.dto.AbilityResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@Tag(name = "Ability", description = "내 활동 및 이력 API")
public interface AbilityControllerDocs {

	@Operation(
		summary = "내 활동 및 이력 조회 API",
		description = "**[최신순 조회]** 커서 기반 페이징을 사용하여, 사용자의 활동 및 이력을 조회합니다. 커서로는 마지막 게시글의 생성 시간을 사용합니다."
	)
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회한 게시글의 생성 시간", example = "2026-02-09T00:00:00"),
		@Parameter(name = "size", description = "한 페이지에 가져올 게시글 개수 (기본 10개)", example = "10")
	})
	ApiResponse<CursorResponseDTO<AbilityResponseDTO.AbilityDTO>> getAbilities(
		@AuthUser Long userId,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	);
}
