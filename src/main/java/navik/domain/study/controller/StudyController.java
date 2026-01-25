package navik.domain.study.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.dto.StudyKpiCardDTO;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.service.StudyCommandService;
import navik.domain.study.service.StudyQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;

@RestController
@RequestMapping("/v1/studies")
@RequiredArgsConstructor
public class StudyController implements StudyControllerDocs {
	private final StudyCommandService studyCommandService;
	private final StudyQueryService studyQueryService;

	/**
	 * 스터디 생성
	 * @param request
	 * @param userId
	 * @return
	 */
	@PostMapping
	public ApiResponse<Long> createStudy(
		@RequestBody @Valid StudyCreateDTO.CreateDTO request,
		@AuthUser Long userId
	) {
		Long studyId = studyCommandService.createStudy(request, userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._CREATED, studyId);
	}

	/**
	 * 나의 스터디 목록 조회
	 * @param role
	 * @param cursor
	 * @param size
	 * @param userId
	 * @return
	 */
	@GetMapping("/my")
	public ApiResponse<CursorResponseDTO<StudyDTO.MyStudyDTO>> getMyStudies(
		@RequestParam(required = false) StudyRole role, // 리더/멤버 탭 구분
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@AuthUser Long userId
	) {
		CursorResponseDTO<StudyDTO.MyStudyDTO> response = studyQueryService.getMyStudyList(userId, role, cursor, size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 직무에 따른 KPI 카드 목록 조회
	 * @param jobName
	 * @param cursor
	 * @param size
	 * @return
	 */
	@GetMapping("/kpi-cards")
	public ApiResponse<CursorResponseDTO<StudyKpiCardDTO.StudyKpiCardNameDTO>> getKpiCards(
		@RequestParam String jobName,
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CursorResponseDTO<StudyKpiCardDTO.StudyKpiCardNameDTO> response =
			studyQueryService.getKpiCardListByJob(jobName, cursor, size);

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}
}
