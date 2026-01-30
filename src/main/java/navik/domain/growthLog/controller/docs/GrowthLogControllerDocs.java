package navik.domain.growthLog.controller.docs;

import java.time.YearMonth;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;
import navik.domain.growthLog.enums.AggregateUnit;
import navik.domain.growthLog.enums.GrowthType;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.PageResponseDto;

@Tag(name = "Growth Log", description = "성장 로그 API")
public interface GrowthLogControllerDocs {

	@Operation(
		summary = "성장 로그 생성",
		description = """
			사용자 입력 성장 로그를 생성하고 AI 평가를 시도합니다.
			- AI 평가 성공: COMPLETED로 저장 + KPI 링크/점수 반영
			- AI 평가 실패: FAILED로 저장되며, 생성 자체는 성공(201)으로 반환됩니다.
			"""
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
	})
	ApiResponse<GrowthLogResponseDTO.CreateResult> create(
		@Parameter(hidden = true) @AuthUser Long userId,
		GrowthLogRequestDTO.CreateUserInput request
	);

	@Operation(
		summary = "성장 로그 재시도",
		description = """
			FAILED 상태의 USER_INPUT 성장 로그에 대해 AI 평가를 재시도합니다.
			- 성공 시: COMPLETED로 업데이트 + KPI 점수 반영
			- 실패 시: 200 OK로 반환되며 status=FAILED 입니다.
			- 제한: 동일 로그에 대해 재시도 횟수 제한이 적용됩니다.
			"""
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재시도 처리 결과 반환"),
	})
	ApiResponse<GrowthLogResponseDTO.RetryResult> retry(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "성장 로그 ID", example = "1", required = true)
		@PathVariable Long growthLogId
	);

	@Operation(
		summary = "월별 성장 로그 목록 조회",
		description = "특정 월에 작성한 성장 로그 목록(요약)을 페이징하여 조회합니다."
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
					name = "월별 성장 로그 목록 조회 성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON200",
						  "message": "성공입니다.",
						  "result": {
							"content": [
							  {
								"growthLogId": 3,
								"title": "와이어프레임 작성",
								"content": "UI 흐름 명확",
								"totalDelta": 2,
								"createdAt": "2025-11-23T15:00:00"
							  },
							  {
								"growthLogId": 2,
								"title": "DB 스터디 3회차 참여",
								"content": "조인 최적화 질문 좋았음",
								"totalDelta": 4,
								"createdAt": "2025-11-11T22:30:00"
							  },
							  {
								"growthLogId": 1,
								"title": "DB 스터디 2회차 참여",
								"content": "피드백: 1:N 관계 표현, 정규화 잘함",
								"totalDelta": 5,
								"createdAt": "2025-11-11T10:00:00"
							  }
							],
							"pageNumber": 0,
							"pageSize": 10,
							"totalPages": 1,
							"totalElements": 3,
							"last": true,
							"nextCursor": null
						  },
						  "timestamp": "2026-01-19T01:17:28"
						}
						"""
				)
			)
		)
	})
	ApiResponse<PageResponseDto<GrowthLogResponseDTO.ListItem>> getMonthlyGrowthLogs(
		@Parameter(hidden = true) @AuthUser Long userId,

		@Parameter(
			description = "조회 연월 (yyyy-MM)",
			example = "2025-11",
			required = true
		)
		@RequestParam YearMonth yearMonth,

		@Parameter(description = "성장 로그 타입", required = false)
		@RequestParam(required = false) GrowthType type,

		@ParameterObject Pageable pageable
	);

	@Operation(
		summary = "성장 로그 상세 조회",
		description = "성장 로그 단건의 상세 정보를 조회합니다."
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
					name = "성장 로그 상세 조회 성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON200",
						  "message": "성공입니다.",
						  "result": {
							"growthLogId": 1,
							"type": "USER_INPUT",
							"title": "DB 스터디 2회차 참여",
							"content": "피드백: 1:N 관계 표현, 정규화 잘함",
							"totalDelta": 5,
							"status": "COMPLETED",
							"createdAt": "2025-11-11T10:00:00",
							"kpiLinks": [
							  {
								"kpiCardId": 11,
								"kpiCardName": "문제 해결 능력",
								"delta": 3
							  },
							  {
								"kpiCardId": 13,
								"kpiCardName": "테스트 작성",
								"delta": 2
							  }
							]
						  },
						  "timestamp": "2026-01-19T01:13:40"
						}
						"""
				)
			)
		)
	})
	ApiResponse<GrowthLogResponseDTO.Detail> getGrowthLogDetail(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "성장 로그 ID", example = "1", required = true)
		@PathVariable Long growthLogId
	);

	@Operation(
		summary = "성장 점수 타임라인 조회",
		description = """
			일/주/월 단위로 성장 점수 누적 타임라인을 조회합니다.
			- 로그가 없으면 빈 리스트를 반환합니다.
			- label 포맷: DAY/WEEK=yyyy-MM-dd, MONTH=yyyy-MM
			"""
	)
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
	})
	ApiResponse<List<GrowthLogResponseDTO.Point>> getGrowthScoreTimeline(
		@Parameter(hidden = true) @AuthUser Long userId,

		@Parameter(
			description = "집계 단위",
			example = "MONTH",
			required = true,
			schema = @Schema(allowableValues = {"DAY", "WEEK", "MONTH"})
		)
		@RequestParam AggregateUnit unit,

		@Parameter(description = "성장 로그 타입", required = false)
		@RequestParam(required = false) GrowthType type
	);
}
