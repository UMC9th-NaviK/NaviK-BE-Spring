package navik.domain.kpi.controller.docs;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.kpi.dto.res.KpiCardResponseDTO;
import navik.domain.kpi.dto.res.KpiCardResponseDTO.GridItem;
import navik.domain.kpi.enums.KpiCardType;
import navik.domain.kpi.exception.code.KpiCardErrorCode;
import navik.domain.users.exception.code.JobErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.swagger.ApiErrorCodes;

@Tag(name = "KPI Card", description = "KPI 카드 조회 API")
public interface KpiCardControllerDocs {

	@Operation(
		summary = "내 직무 기준 KPI 카드 목록 조회",
		description = """
			**[4-1 리포트 > 내 카드 > 전체 KPI 카드]**
			
			인증된 사용자의 직무(job)를 기준으로 KPI 카드 그리드 목록을 조회합니다.
			"""

	)
	@ApiErrorCodes(
		enumClass = JobErrorCode.class,
		includes = {"JOB_NOT_ASSIGNED"}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = {
					@io.swagger.v3.oas.annotations.media.ExampleObject(
						name = "KPI 카드 그리드 조회 예시",
						summary = "카드 그리드 조회용 응답",
						value = """
							{
							    "isSuccess": true,
							    "code": "COMMON200",
							    "message": "성공입니다.",
							    "result": [
							        {
							            "kpiCardId": 11,
							            "name": "문제 해결 능력",
							            "imageUrl": "https://cdn.navik.co.kr/kpi/problem-solving.png"
							        },
							        {
							            "kpiCardId": 12,
							            "name": "코드 품질",
							            "imageUrl": "https://cdn.navik.co.kr/kpi/problem-solving.png"
							        },
							        {
							            "kpiCardId": 13,
							            "name": "테스트 작성",
							            "imageUrl": "https://cdn.navik.co.kr/kpi/problem-solving.png"
							        }
							    ],
							    "timestamp": "2026-01-31T18:07:22"
							}
							"""
					)
				}
			)
		)
	})
	ApiResponse<List<GridItem>> getMyKpiCards(
		@Parameter(hidden = true)
		@AuthUser Long userId
	);

	@Operation(
		summary = "KPI 카드 목록 조회",
		description = """
			**[4-1 리포트 > 내 카드 > 전체 KPI 카드]**
			
			jobId에 해당하는 KPI 카드 그리드 목록을 조회합니다.
			"""
	)
	@ApiErrorCodes(
		enumClass = JobErrorCode.class,
		includes = {"JOB_NOT_FOUND"}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = {
					@io.swagger.v3.oas.annotations.media.ExampleObject(
						name = "KPI 카드 그리드 조회 예시",
						summary = "jobId 기반 카드 그리드 조회용 응답",
						value = """
							{
							  "data": [
							    {
							      "id": 10,
							      "name": "문제 해결 능력",
							      "imageUrl": "https://cdn.navik.co.kr/kpi/problem-solving.png"
							    },
							    {
							      "id": 11,
							      "name": "협업 능력",
							      "imageUrl": "https://cdn.navik.co.kr/kpi/collaboration.png"
							    }
							  ]
							}
							"""
					)
				}
			)
		)
	})
	ApiResponse<List<GridItem>> getKpiCards(
		@Parameter(description = "직무 ID", example = "1", required = true)
		@RequestParam Long jobId
	);

	@Operation(
		summary = "KPI 카드 상세 조회(타입별)",
		description = """
			**[4-1 리포트/내카드/카드 클릭]**
			
			kpiCardId와 type(strong/weak)에 해당하는 KPI 카드 상세 정보를 조회합니다.
			리포트 또는 내 카드 화면에서 핵심역량/극복역량 목록을 조회한 뒤,
			선택한 카드의 id와 type을 사용하여 상세 조회할 수 있습니다.
			"""
	)
	@ApiErrorCodes(
		enumClass = KpiCardErrorCode.class,
		includes = {
			"INVALID_KPI_CARD_TYPE",
			"KPI_CARD_NOT_FOUND"
		}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = {
					@io.swagger.v3.oas.annotations.media.ExampleObject(
						name = "strong 상세 조회 예시",
						summary = "strong type",
						value = """
							{
							    "isSuccess": true,
							    "code": "COMMON200",
							    "message": "성공입니다.",
							    "result": {
							        "kpiCardId": 11,
							        "name": "문제 해결 능력",
							        "content": {
							            "title": "좋은 분석",
							            "content": "분석을 굉장히 잘하시네요."
							        },
							        "imageUrl": "https://cdn.navik.co.kr/kpi/problem-solving.png"
							    },
							    "timestamp": "2026-01-31T18:14:27"
							}
							"""
					),
					@io.swagger.v3.oas.annotations.media.ExampleObject(
						name = "weak 상세 조회 예시",
						summary = "weak type",
						value = """
							{
							    "isSuccess": true,
							    "code": "COMMON200",
							    "message": "성공입니다.",
							    "result": {
							        "kpiCardId": 11,
							        "name": "문제 해결 능력",
							        "content": {
							            "title": "과도한 분석",
							            "content": "분석에 시간이 오래 걸릴 수 있습니다."
							        },
							        "imageUrl": "https://cdn.navik.co.kr/kpi/problem-solving.png"
							    },
							    "timestamp": "2026-01-31T18:14:27"
							}
							"""
					)
				}
			)
		)
	})
	ApiResponse<KpiCardResponseDTO.Detail> getKpiCardDetail(
		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId,

		@Parameter(
			description = "카드 타입 (strong | weak)",
			required = true,
			schema = @Schema(allowableValues = {"strong", "weak"}),
			example = "strong"
		)
		@RequestParam KpiCardType type
	);

	@Operation(
		summary = "KPI 카드 상세 조회(전체)",
		description = """
			**[4-1 리포트 > 내 카드]**
			
			kpiCardId에 해당하는 KPI 카드의 strong/weak 상세 정보를 모두 조회합니다.
			리포트/내 카드 화면에서 선택한 KPI 카드의 id로 상세 화면 진입 시 사용합니다.
			※ imageUrl은 이미지 미적용 상태에서는 null로 반환될 수 있습니다.
			"""
	)
	@ApiErrorCodes(
		enumClass = KpiCardErrorCode.class,
		includes = {"KPI_CARD_NOT_FOUND"}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
					name = "KPI 카드 상세 조회 응답",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON200",
						  "message": "성공입니다.",
						  "result": {
							"kpiCardId": 11,
							"name": "문제 해결 능력",
							"content": {
							  "strong": {
								"title": "문제 구조화",
								"content": "복잡한 문제를 체계적으로 정리합니다."
							  },
							  "weak": {
								"title": "과도한 분석",
								"content": "분석에 시간이 오래 걸릴 수 있습니다."
							  }
							},
							"imageUrl": null
						  },
						  "timestamp": "2026-01-31T18:14:27"
						}
						"""
				)
			)
		)
	})
	ApiResponse<KpiCardResponseDTO.AllDetail> getKpiCardAllDetail(
		@Parameter(description = "KPI 카드 ID", example = "10", required = true)
		@PathVariable Long kpiCardId
	);

	@Operation(
		summary = "상위 3개 KPI 카드 조회",
		description = """
			**[4-1 리포트 > 내 카드 > 핵심 역량]**
			
			사용자 기준 점수가 높은 KPI 카드 3개를 조회합니다.
			메인/대시보드 화면에서 상위 KPI 카드 영역 렌더링에 사용합니다.
			"""
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
					name = "상위 KPI 카드 조회 응답",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON200",
						  "message": "성공입니다.",
						  "result": [
							{
							  "kpiCardId": 3,
							  "name": "커뮤니케이션",
							  "score": 85,
							  "imageUrl": null
							},
							{
							  "kpiCardId": 7,
							  "name": "문제 해결 능력",
							  "score": 78,
							  "imageUrl": null
							},
							{
							  "kpiCardId": 1,
							  "name": "자기 주도성",
							  "score": 72,
							  "imageUrl": null
							}
						  ],
						  "timestamp": "2026-01-31T18:14:27"
						}
						"""
				)
			)
		)
	})
	ApiResponse<List<KpiCardResponseDTO.GridItem>> top(
		@Parameter(hidden = true) @AuthUser Long userId
	);

	@Operation(
		summary = "하위 3개 KPI 카드 조회",
		description = """
			**[4-1 리포트 > 내 카드 > 극복 역량]**
			
			사용자 기준 점수가 낮은 KPI 카드 3개를 조회합니다.
			메인/대시보드 화면에서 개선 필요 KPI 카드 영역 렌더링에 사용합니다.
			"""
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "성공",
			content = @io.swagger.v3.oas.annotations.media.Content(
				mediaType = "application/json",
				examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
					name = "하위 KPI 카드 조회 응답",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON200",
						  "message": "성공입니다.",
						  "result": [
							{
							  "kpiCardId": 9,
							  "name": "시간 관리",
							  "score": 32,
							  "imageUrl": null
							},
							{
							  "kpiCardId": 5,
							  "name": "우선순위 설정",
							  "score": 28,
							  "imageUrl": null
							},
							{
							  "kpiCardId": 12,
							  "name": "집중력",
							  "score": 21,
							  "imageUrl": null
							}
						  ],
						  "timestamp": "2026-01-31T18:14:27"
						}
						"""
				)
			)
		)
	})
	ApiResponse<List<KpiCardResponseDTO.GridItem>> bottom(
		@Parameter(hidden = true) @AuthUser Long userId
	);

}
