package navik.domain.study.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import navik.domain.study.dto.StudyApplicationDTO;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.dto.StudyKpiCardDTO;
import navik.domain.study.dto.StudyRecommendDTO;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.service.StudyCommandService;
import navik.domain.study.service.StudyQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDTO;
import navik.global.swagger.SwaggerPageable;

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
	@SwaggerPageable
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
	@SwaggerPageable
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

	/**
	 * 맞춤형 스터디 추천 목록 조회
	 * @param cursor
	 * @param size
	 * @param userId
	 * @return
	 */
	@SwaggerPageable
	@GetMapping("/recommendation")
	public ApiResponse<CursorResponseDTO<StudyRecommendDTO>> getRecommendedStudies(
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@AuthUser Long userId
	) {
		CursorResponseDTO<StudyRecommendDTO> response = studyQueryService.getRecommendedStudyList(userId, cursor, size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 스터디 신청하기 버튼 클릭
	 * @param studyId
	 * @param userId
	 * @return
	 */
	@PostMapping("/{studyId}/apply")
	public ApiResponse<String> applyStudy(
		@PathVariable Long studyId,
		@AuthUser Long userId
	) {
		studyCommandService.studyApply(userId, studyId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK);
	}

	/**
	 * 스터디 신청 현황 목록 조회 (스터디장)
	 * @param studyId
	 * @param cursor
	 * @param size
	 * @return
	 */
	@SwaggerPageable
	@GetMapping("/{studyId}/applicants")
	public ApiResponse<CursorResponseDTO<StudyApplicationDTO.ApplicationPreviewDTO>> getApplicants(
		@PathVariable Long studyId,
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CursorResponseDTO<StudyApplicationDTO.ApplicationPreviewDTO> response = studyQueryService.getApplicantList(
			studyId, cursor, size);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, response);
	}

	/**
	 * 신청 수락/거절 처리
	 * @param studyUserId
	 * @param request
	 * @return
	 */
	@PatchMapping("/applicants/{studyUserId}")
	public ApiResponse<String> processApply(
		@AuthUser Long userId,
		@PathVariable Long studyUserId,
		@RequestBody @Valid StudyApplicationDTO.ProcessApplicationDTO request
	) {
		studyCommandService.resolveApplication(userId, studyUserId, request.getAccept());
		return ApiResponse.onSuccess(GeneralSuccessCode._OK);
	}
}
