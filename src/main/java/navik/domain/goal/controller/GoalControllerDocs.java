package navik.domain.goal.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.goal.dto.GoalRequestDTO;
import navik.domain.goal.dto.GoalResponseDTO;
import navik.domain.goal.entity.GoalStatus;
import navik.global.apiPayload.ApiResponse;
import navik.global.dto.CursorResponseDto;

@Tag(name = "Goal", description = "목표 관련 API")
public interface GoalControllerDocs {
	@Operation(summary = "내 목표", description = """
        사용자가 설정한 목표 목록을 조회합니다.
        
        **정렬 옵션 (sortBy)**
        - `RECENT`: 최근 작성한 순서 (기본값)
        - `DEADLINE`: 마감일이 가까운 순서
        
        **커서 기반 페이징**
        - 첫 요청: cursor 없이 호출 → 첫 페이지 반환
        - 다음 요청: 응답의 `nextCursor` 값을 cursor 파라미터에 전달
        - size 기본값: 10
        - `hasNext`가 false면 마지막 페이지입니다
        """)
	ApiResponse<CursorResponseDto<GoalResponseDTO.PreviewDTO>> getGoals(Long userId,
		@Parameter(description = "마지막으로 조회한 목표 ID (nextCursor)") Long cursor,
		@Parameter(description = "한번에 가져올 데이터 수", example = "10") Integer size,
		@Parameter(description = "정렬 기준", example = "RECENT") String sortBy);

	@Operation(summary = "목표 설정", description = """
        사용자의 새로운 목표를 생성합니다.
        - 처음 생성 시 상태는 기본적으로 `NONE`(미정)으로 설정됩니다.
        """)
	ApiResponse<GoalResponseDTO.InfoDTO> createGoal(Long userId, GoalRequestDTO.CreateDTO req);

	@Operation(summary = "목표 상태 변경", description = "목표의 진행 상태를 변경합니다.")
	ApiResponse<GoalResponseDTO.InfoDTO> updateGoalStatus(Long goalId, GoalStatus status);

	@Operation(summary = "목표 삭제", description = "목표를 삭제합니다.")
	ApiResponse<Void> deleteGoal (Long goalId);
}