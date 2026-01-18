package navik.domain.recruitment.controller.position;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.recruitment.dto.position.PositionRequestDTO;
import navik.domain.recruitment.dto.position.PositionResponseDTO;
import navik.global.apiPayload.ApiResponse;
import navik.global.dto.CursorResponseDto;

@Tag(name = "Position", description = "채용 공고 중 포지션 관련 API")
public interface PositionControllerDocs {

	@Operation(summary = "사용자 추천 포지션 전체 검색", description = """
		사용자에게 적합한 포지션을 전체 검색합니다.
		
		**검색 필터 조건**
		- 특정 필터를 적용하지 않고 무관하게 모두 본다면, 해당 필터의 List는 안보내시면 됩니다!
		
		**커서 기반 페이징**
		- 첫 요청: cursor 없이 호출 → 첫 페이지 반환
		- 다음 요청: 응답의 `nextCursor` 값을 cursor 파라미터에 전달
		- size 기본값: 10
		- `hasNext`가 false면 마지막 페이지입니다.
		""")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		{
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
		})
	ApiResponse<CursorResponseDto<PositionResponseDTO.RecommendedPosition>> getPositions(
		Long userId,
		@ParameterObject PositionRequestDTO.SearchCondition searchCondition,
		@Parameter(description = "마지막으로 조회한 커서 (nextCursor)") String cursor,
		@Parameter(description = "한번에 가져올 데이터 수", example = "10") Integer size
	);
}
