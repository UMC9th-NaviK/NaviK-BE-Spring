package navik.domain.recruitment.controller.position;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.domain.recruitment.service.position.PositionQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recruitments/positions")
public class PositionController implements PositionControllerDocs {

	private final PositionQueryService positionQueryService;

	/**
	 * 검색 조건이 매우 많아 의도적으로 PostMapping, json으로 조건을 받습니다.
	 */
	@Override
	@PostMapping
	public ApiResponse<CursorResponseDTO<PositionResponseDTO.RecommendedPosition>> getPositions(
		@AuthUser Long userId,
		@RequestBody PositionRequestDTO.SearchCondition searchCondition,
		@RequestParam(required = false) String cursor,
		@RequestParam(defaultValue = "10") Integer size
	) {
		Pageable pageable = PageRequest.of(0, size);
		CursorResponseDTO<PositionResponseDTO.RecommendedPosition> result = positionQueryService.getPositions(userId,
			searchCondition, cursor, pageable);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}

	@Override
	@PostMapping("/count")
	public ApiResponse<Long> getPositionCount(@RequestBody PositionRequestDTO.SearchCondition searchCondition) {
		Long result = positionQueryService.getPositionCount(searchCondition);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}
}
