package navik.domain.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "Portfolio", description = "포트폴리오 관련 API")
public interface PortfolioControllerDocs {

	@Operation(summary = "포트폴리오 등록 및 분석 요청", description = """
		포트폴리오를 등록하고 AI 분석을 비동기로 요청합니다.
		
		### 입력 방식 (inputType)
		1. **TEXT**: `content` 필드에 이력서 텍스트를 직접 입력합니다.
		2. **PDF**: S3 Presigned URL을 통해 파일을 업로드한 후, `fileUrl` 필드에 해당 파일의 Key(경로)를 입력합니다.
		
		### 비동기 처리 안내
		- 본 API는 포트폴리오 저장 성공 시 즉시 응답을 반환합니다.
		- **AI 분석(KPI 도출)**은 백그라운드(Redis Stream)에서 별도로 진행되며, 완료 후 결과가 업데이트됩니다.
		""")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "포트폴리오 등록 성공 및 분석 작업 예약됨")})
	ApiResponse<PortfolioResponseDto.Created> registerPortfolio(@AuthUser Long userId,
		@Valid @RequestBody PortfolioRequestDto.Create request);

}
