package navik.domain.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.portfolio.dto.PortfolioRequestDto;
import navik.domain.portfolio.dto.PortfolioResponseDto;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;

@Tag(name = "Portfolio", description = "포트폴리오 관련 API")
public interface PortfolioControllerDocs {

	@Operation(summary = "포트폴리오 생성", description = """
		포트폴리오 정보를 저장합니다.
		
		- inputType: TEXT(텍스트 직접 입력), PDF(파일 업로드)
		- TEXT인 경우: content 필드에 텍스트 입력
		- PDF인 경우: S3 presigned URL로 업로드 후 fileUrl 필드에 key 입력
		""")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")})
	ApiResponse<PortfolioResponseDto.Created> createPortfolio(@AuthUser Long userId,
		PortfolioRequestDto.Create request);

}
