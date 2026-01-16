package navik.domain.term.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.term.dto.TermRequestDTO;
import navik.domain.term.dto.TermResponseDTO;
import navik.global.apiPayload.ApiResponse;

@Tag(name = "Term", description = "약관 관련 API")
public interface TermControllerDocs {

	@Operation(summary = "약관 상세 조회", description = "특정 약관의 상세 내용을 조회합니다.")
	ApiResponse<TermResponseDTO.TermInfo> getTerm(Long termId);

	@Operation(summary = "약관 동의", description = "선택한 약관들에 대해 일괄 동의합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")})
	ApiResponse<TermResponseDTO.AgreementResultDTO> agreeTerms(Long userId, TermRequestDTO.AgreeDTO req);
}
