package navik.domain.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.portfolio.dto.PortfolioRequestDTO;
import navik.domain.portfolio.dto.PortfolioResponseDTO;
import navik.global.auth.annotation.AuthUser;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Portfolio", description = "포트폴리오 관련 API")
public interface PortfolioControllerDocs {

	@Operation(
		summary = "포트폴리오 등록 및 분석 요청",
		description = """
			포트폴리오를 등록하고 AI 분석을 비동기로 요청합니다.
			
			### 입력 방식 (inputType)
			1. **TEXT**: `content` 필드에 이력서 텍스트를 직접 입력합니다.
			2. **PDF**: S3 Presigned URL을 통해 파일을 업로드한 후, `fileUrl` 필드에 해당 파일의 Key(경로)를 입력합니다.
			
			### 비동기 처리 안내
			- 본 API는 포트폴리오 저장 성공 시 즉시 응답을 반환합니다.
			- **AI 분석(KPI 도출)**은 백그라운드(Redis Stream)에서 별도로 진행되며, 완료 후 결과가 업데이트됩니다.

			### 분석 결과 상태
			- **COMPLETED**: 모든 KPI 점수가 정상 반영됨
			- **RETRY_REQUIRED**: 일부 KPI의 basis가 none으로 판정되어 해당 항목은 0점 처리됨. 추가 정보 제출(submitAdditionalInfo)을 통해 재분석 필요
			- **FAILED**: 분석 자체가 실패함 (빈 응답 등)
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "포트폴리오 등록 성공",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "포트폴리오 등록 성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON_201",
						  "message": "요청이 성공적으로 처리되었습니다.",
						  "result": {
						    "id": 1,
						    "inputType": "TEXT"
						  },
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "입력값 검증 실패",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "입력 타입 누락",
					summary = "inputType이 null인 경우",
					value = """
						{
						  "isSuccess": false,
						  "code": "COMMON_400",
						  "message": "입력 타입은 필수입니다.",
						  "result": null,
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "인증 실패",
					value = """
						{
						  "isSuccess": false,
						  "code": "AUTH_401_01",
						  "message": "인증에 실패했습니다.",
						  "result": null,
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "403",
			description = "온보딩 미완료",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "온보딩 미완료",
					summary = "사용자 상태가 ACTIVE가 아닌 경우",
					value = """
						{
						  "isSuccess": false,
						  "code": "AUTH_403_02",
						  "message": "온보딩을 완료해주세요.",
						  "result": null,
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		)
	})
	navik.global.apiPayload.ApiResponse<PortfolioResponseDTO.Created> registerPortfolio(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Valid @io.swagger.v3.oas.annotations.parameters.RequestBody PortfolioRequestDTO.Create request
	);

	@Operation(
		summary = "추가 정보 제출 (재분석 필요 시)",
		description = """
			AI 분석 결과에서 일부 KPI의 basis가 none으로 판정된 포트폴리오에 대해 추가 정보를 제출하고 재분석을 요청합니다.

			### 조건
			- 포트폴리오 상태가 **RETRY_REQUIRED**인 경우에만 호출 가능합니다.
			- 초기 분석에서 basis가 유효한 KPI 점수는 이미 반영되어 있으며, basis="none"인 항목만 0점으로 남아있습니다.
			- 추가 정보(qB1~qB5)를 입력하면 fallback 분석이 진행되어 0점인 항목만 업데이트됩니다.

			### 비동기 처리 안내
			- 추가 정보 저장 후 재분석이 비동기로 진행됩니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "추가 정보 제출 성공",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "추가 정보 제출 성공 예시",
					value = """
						{
						  "isSuccess": true,
						  "code": "COMMON_200",
						  "message": "요청이 성공적으로 처리되었습니다.",
						  "result": {
						    "portfolioId": 1
						  },
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = {
					@ExampleObject(
						name = "입력값 검증 실패",
						summary = "qB1~qB5 중 null인 필드가 있는 경우",
						value = """
							{
							  "isSuccess": false,
							  "code": "COMMON_400",
							  "message": "qB1은 필수입니다.",
							  "result": null,
							  "timestamp": "2026-02-05T07:00:00"
							}
							"""
					),
					@ExampleObject(
						name = "잘못된 포트폴리오 상태",
						summary = "포트폴리오 상태가 RETRY_REQUIRED가 아닌 경우",
						value = """
							{
							  "isSuccess": false,
							  "code": "PORTFOLIO_400_01",
							  "message": "재분석이 필요한 상태에서만 추가 정보를 입력할 수 있습니다.",
							  "result": null,
							  "timestamp": "2026-02-05T07:00:00"
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "인증 실패",
					value = """
						{
						  "isSuccess": false,
						  "code": "AUTH_401_01",
						  "message": "인증에 실패했습니다.",
						  "result": null,
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "403",
			description = "접근 권한 없음",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = {
					@ExampleObject(
						name = "온보딩 미완료",
						summary = "사용자 상태가 ACTIVE가 아닌 경우",
						value = """
							{
							  "isSuccess": false,
							  "code": "AUTH_403_02",
							  "message": "온보딩을 완료해주세요.",
							  "result": null,
							  "timestamp": "2026-02-05T07:00:00"
							}
							"""
					),
					@ExampleObject(
						name = "포트폴리오 접근 권한 없음",
						summary = "다른 사용자의 포트폴리오에 접근하는 경우",
						value = """
							{
							  "isSuccess": false,
							  "code": "PORTFOLIO_403_01",
							  "message": "해당 포트폴리오에 접근 권한이 없습니다.",
							  "result": null,
							  "timestamp": "2026-02-05T07:00:00"
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "포트폴리오 없음",
			content = @Content(
				schema = @Schema(implementation = navik.global.apiPayload.ApiResponse.Body.class),
				examples = @ExampleObject(
					name = "포트폴리오 없음",
					summary = "존재하지 않는 portfolioId",
					value = """
						{
						  "isSuccess": false,
						  "code": "PORTFOLIO_404_01",
						  "message": "포트폴리오를 찾을 수 없습니다.",
						  "result": null,
						  "timestamp": "2026-02-05T07:00:00"
						}
						"""
				)
			)
		)
	})
	navik.global.apiPayload.ApiResponse<PortfolioResponseDTO.AdditionalInfoSubmitted> submitAdditionalInfo(
		@Parameter(hidden = true) @AuthUser Long userId,
		@Parameter(description = "포트폴리오 ID", example = "1", required = true) @PathVariable Long portfolioId,
		@Valid @io.swagger.v3.oas.annotations.parameters.RequestBody PortfolioRequestDTO.AdditionalInfo request
	);
}
