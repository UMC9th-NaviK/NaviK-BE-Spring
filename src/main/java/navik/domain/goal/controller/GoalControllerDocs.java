package navik.domain.goal.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.goal.dto.GoalRequestDTO;
import navik.domain.goal.dto.GoalResponseDTO;
import navik.domain.goal.entity.GoalStatus;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@Tag(name = "Goal", description = "목표 관련 API")
public interface GoalControllerDocs {

	@Operation(summary = "목표 단건 조회", description = "목표의 상세 정보를 조회합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "목표 단건 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "goalId": 1,
			    "title": "스프링 부트 마스터하기",
			    "content": "스프링 부트 핵심 원리와 활용법을 학습한다.",
			    "endDate": "2026-06-30",
			    "status": "IN_PROGRESS"
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
			@ExampleObject(name = "다른 사용자의 목표", summary = "본인의 목표가 아닌 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_01",
				  "message": "접근 권한이 없습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				"""),
			@ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_02",
				  "message": "온보딩이 완료되지 않았습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				""")})),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "목표 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 목표", summary = "해당 goalId에 대한 목표가 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_404",
			  "message": "해당 리소스를 찾을 수 없습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<GoalResponseDTO.InfoDTO> getGoal(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "조회할 목표 ID", example = "1", required = true) @PathVariable Long goalId);

	@Operation(summary = "목표 수정", description = "목표의 제목, 내용, 마감일을 수정합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "목표 수정 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "goalId": 1,
			    "title": "스프링 부트 완전 정복",
			    "content": "스프링 부트 핵심 원리와 실전 프로젝트를 진행한다.",
			    "endDate": "2026-09-30",
			    "status": "IN_PROGRESS"
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "필수 값 누락", summary = "title, content, endDate 중 누락된 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_400_01",
			  "message": "입력값이 올바르지 않습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
			@ExampleObject(name = "다른 사용자의 목표", summary = "본인의 목표가 아닌 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_01",
				  "message": "접근 권한이 없습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				"""),
			@ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_02",
				  "message": "온보딩이 완료되지 않았습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				""")})),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "목표 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 목표", summary = "해당 goalId에 대한 목표가 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_404",
			  "message": "해당 리소스를 찾을 수 없습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<GoalResponseDTO.InfoDTO> updateGoal(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "수정할 목표 ID", example = "1", required = true) @PathVariable Long goalId,
		@RequestBody @Valid GoalRequestDTO.UpdateInfoDTO req);

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
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "내 목표 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "content": [
			      {
			        "goalId": 1,
			        "content": "스프링 부트 마스터하기",
			        "status": "IN_PROGRESS"
			      },
			      {
			        "goalId": 2,
			        "content": "알고리즘 100문제 풀기",
			        "status": "NONE"
			      }
			    ],
			    "pageSize": 2,
			    "nextCursor": "2",
			    "hasNext": true
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 정렬 기준", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "잘못된 sortBy 값", summary = "RECENT, DEADLINE 이외의 정렬 기준을 입력한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "SORT_400",
			  "message": "지원하지 않는 정렬 기준입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_403_02",
			  "message": "온보딩이 완료되지 않았습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<CursorResponseDTO<GoalResponseDTO.PreviewDTO>> getGoals(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "마지막으로 조회한 목표 ID (nextCursor)") @RequestParam(required = false) Long cursor,
		@Parameter(description = "한번에 가져올 데이터 수", example = "10") @RequestParam(defaultValue = "10") Integer size,
		@Parameter(description = "정렬 기준", example = "RECENT") @RequestParam String sortBy);

	@Operation(summary = "목표 설정", description = """
		사용자의 새로운 목표를 생성합니다.
		- 처음 생성 시 상태는 기본적으로 `NONE`(미정)으로 설정됩니다.
		""")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "목표 설정 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON201",
			  "message": "요청 성공 및 리소스 생성됨",
			  "result": {
			    "goalId": 1,
			    "content": "스프링 부트 마스터하기",
			    "endDate": "2026-06-30",
			    "status": "NONE"
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "필수 값 누락", summary = "content, endDate 중 누락된 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_400_01",
			  "message": "입력값이 올바르지 않습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_403_02",
			  "message": "온보딩이 완료되지 않았습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<GoalResponseDTO.InfoDTO> createGoal(
		@Parameter(hidden = true) @AuthUser Long userId,
		@RequestBody @Valid GoalRequestDTO.CreateDTO req);

	@Operation(summary = "목표 상태 변경", description = "목표의 진행 상태를 변경합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "목표 상태 변경 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": {
			    "goalId": 1,
			    "content": "스프링 부트 마스터하기",
			    "endDate": "2026-06-30",
			    "status": "COMPLETED"
			  },
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
			@ExampleObject(name = "다른 사용자의 목표", summary = "본인의 목표가 아닌 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_01",
				  "message": "접근 권한이 없습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				"""),
			@ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_02",
				  "message": "온보딩이 완료되지 않았습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				""")})),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "목표 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 목표", summary = "해당 goalId에 대한 목표가 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_404",
			  "message": "해당 리소스를 찾을 수 없습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<GoalResponseDTO.InfoDTO> updateGoalStatus(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "상태를 변경할 목표 ID", example = "1", required = true) @PathVariable Long goalId,
		@Parameter(description = "변경할 상태", example = "COMPLETED") @RequestParam GoalStatus status);

	@Operation(summary = "목표 삭제", description = "목표를 삭제합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
			@ExampleObject(name = "다른 사용자의 목표", summary = "본인의 목표가 아닌 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_01",
				  "message": "접근 권한이 없습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				"""),
			@ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
				{
				  "isSuccess": false,
				  "code": "AUTH_403_02",
				  "message": "온보딩이 완료되지 않았습니다.",
				  "result": null,
				  "timestamp": "2026-01-20T14:30:00"
				}
				""")})),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "목표 없음", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "존재하지 않는 목표", summary = "해당 goalId에 대한 목표가 없는 경우", value = """
			{
			  "isSuccess": false,
			  "code": "COMMON_404",
			  "message": "해당 리소스를 찾을 수 없습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<Void> deleteGoal(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "삭제할 목표 ID", example = "1", required = true) @PathVariable Long goalId);

	@Operation(summary = "진행 중인 목표 조회", description = """
		진행 중(IN_PROGRESS) 상태의 목표 목록을 조회합니다.
		- 마감일이 가까운 순서로 정렬됩니다.
		""")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "진행 중인 목표 조회 성공 예시", value = """
			{
			  "isSuccess": true,
			  "code": "COMMON200",
			  "message": "성공입니다.",
			  "result": [
			    {
			      "goalId": 1,
			      "content": "스프링 부트 마스터하기",
			      "status": "IN_PROGRESS"
			    },
			    {
			      "goalId": 3,
			      "content": "JPA 최적화 공부하기",
			      "status": "IN_PROGRESS"
			    }
			  ],
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "인증 실패", summary = "토큰이 없거나 유효하지 않은 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_401_01",
			  "message": "인증되지 않은 사용자입니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "PENDING 사용자 접근", summary = "온보딩을 완료하지 않은 PENDING 상태의 사용자가 접근한 경우", value = """
			{
			  "isSuccess": false,
			  "code": "AUTH_403_02",
			  "message": "온보딩이 완료되지 않았습니다.",
			  "result": null,
			  "timestamp": "2026-01-20T14:30:00"
			}
			""")))})
	ApiResponse<GoalResponseDTO.InProgressDTO> getInProgressGoals(
		@Parameter(hidden = true) @AuthUser Long userId);
}
