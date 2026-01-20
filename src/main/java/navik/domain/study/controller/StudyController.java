package navik.domain.study.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.service.StudyCommandService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequestMapping("/v1/studies")
@RequiredArgsConstructor
public class StudyController implements StudyControllerDocs {
	private final StudyCommandService studyCommandService;

	/**
	 * 스터디 생성
	 * @param request
	 * @param userId
	 * @return
	 */
	@PostMapping
	public ApiResponse<Long> createStudy(
		@RequestParam @Valid StudyCreateDTO.CreateDTO request,
		@AuthUser Long userId
	) {
		Long studyId = studyCommandService.createStudy(request);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, studyId);
	}
}
