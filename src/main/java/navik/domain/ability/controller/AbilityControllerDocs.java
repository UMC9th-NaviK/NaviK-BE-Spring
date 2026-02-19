package navik.domain.ability.controller;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.ability.dto.AbilityResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;
import navik.global.swagger.annotation.ApiErrorCodes;

@Tag(name = "Ability", description = "내 활동 및 이력 API")
public interface AbilityControllerDocs {

	@Operation(
		summary = "내 활동 및 이력 조회 API",
		description = "**[최신순 조회]** 커서 기반 페이징을 사용하여, 사용자의 활동 및 이력을 조회합니다."
	)
	@Parameters({
		@Parameter(name = "cursor", description = "Base64로 인코딩된 값입니다. 그대로 넘겨주세요. (첫 조회 시에는 X)", example = "MjAyNC0wOC0wMVQxMjowMDowMF8y"),
		@Parameter(name = "size", description = "한 페이지에 가져올 항목의 개수 (기본 10개)", example = "10")
	})
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.class),
			examples = @ExampleObject(value = """
				{
				    "isSuccess": true,
				    "code": "COMMON_200",
				    "message": "성공입니다.",
				    "result": {
				        "content": [
				            {
				                "abilityId": 1,
				                "content": "Java 및 Spring을 활용한 백엔드 개발 역량"
				            },
				            {
				                "abilityId": 2,
				                "content": "임베딩 기반 추천 시스템 설계 경험"
				            }
				        ],
				        "hasNext": true,
				        "nextCursor": "MjAyNC0wOC0wMVQxMjowMDowMF8y"
				    }
				}
				""")))
	})
	@ApiErrorCodes(
		enumClass = GeneralErrorCode.class,
		includes = {
			"USER_NOT_FOUND",
			"INVALID_INPUT_VALUE"
		}
	)
	ApiResponse<CursorResponseDTO<AbilityResponseDTO.AbilityDTO>> getAbilities(
		@AuthUser Long userId,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	);

	@Operation(
		summary = "내 활동 및 이력 삭제 API",
		description = "현재까지 등록되어있는 `내 활동 및 이력`을 모두 삭제합니다."
	)
	ApiResponse<Void> deleteAbilities(@AuthUser Long userId);
}
