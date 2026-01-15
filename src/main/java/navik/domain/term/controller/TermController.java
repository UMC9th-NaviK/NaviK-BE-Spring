package navik.domain.term.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.term.dto.TermRequestDTO;
import navik.domain.term.dto.TermResponseDTO;
import navik.domain.term.service.TermQueryService;
import navik.domain.term.service.UserTermCommandService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/terms")
public class TermController implements TermControllerDocs {

	private final TermQueryService termQueryService;
	private final UserTermCommandService userTermCommandService;

	@Override
	@GetMapping("/{termId}")
	public ApiResponse<TermResponseDTO.TermInfo> getTerm(@PathVariable Long termId) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, termQueryService.getTermInfo(termId));
	}

	@Override
	@PostMapping("/agree")
	public ApiResponse<TermResponseDTO.AgreementResultDTO> agreeTerms(
		@AuthUser Long userId,
		@RequestBody @Valid TermRequestDTO.AgreeDTO req) {
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED,
			userTermCommandService.agreeTerms(userId, req.termIds()));
	}
}
