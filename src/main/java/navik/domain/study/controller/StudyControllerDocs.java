package navik.domain.study.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import navik.domain.study.dto.StudyApplicationDTO;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.dto.StudyKpiCardDTO;
import navik.domain.study.dto.StudyRecommendDTO;
import navik.domain.study.enums.StudyRole;
import navik.domain.study.exception.code.StudyErrorCode;
import navik.global.apiPayload.ApiResponse;
import navik.global.auth.annotation.AuthUser;
import navik.global.dto.CursorResponseDto;
import navik.global.swagger.ApiErrorCodes;

@Tag(name = "Study", description = "스터디 관련 API")
public interface StudyControllerDocs {
	@Operation(summary = "스터디 생성 API", description = "새로운 스터디를 생성합니다.")
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {"USER_NOT_FOUND"}
	)
	ApiResponse<Long> createStudy(
		@RequestBody @Valid StudyCreateDTO.CreateDTO request,
		@AuthUser Long userId
	);

	@Operation(summary = "나의 스터디 목록 조회 API", description = "커서 기반 페이징으로 나의 스터디를 조회합니다.")
	@Parameters({
		@Parameter(name = "role", description = "필터링할 역할 (STUDY_LEADER: 내가 만든 스터디, STUDY_MEMBER: 내가 참여한 스터디)", example = "STUDY_LEADER"),
		@Parameter(name = "cursor", description = "마지막으로 조회된 스터디 유저 ID (StudyUser의 PK)", example = "50"),
		@Parameter(name = "size", description = "한 번에 조회할 스터디 개수", example = "10")
	})
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {"USER_NOT_FOUND"}
	)
	ApiResponse<CursorResponseDto<StudyDTO.MyStudyDTO>> getMyStudies(
		@RequestParam(required = false) StudyRole role, // 리더/멤버 탭 구분
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@AuthUser Long userId
	);

	@Operation(summary = "직무별 KPI 카드 목록 조회 API", description = "스터디 생성 시 특정 직무를 선택했을 때 해당되는 KPI 카드 리스트를 조회합니다. 커서 기반 페이징(무한 스크롤)을 지원합니다.")
	@Parameters({
		@Parameter(name = "jobName", description = "조회할 직무의 이름 (예: 기획, 개발, 디자인 등)", example = "개발"),
		@Parameter(name = "cursor", description = "마지막으로 조회된 KPI 카드의 ID.", example = "10"),
		@Parameter(name = "size", description = "한 번에 조회할 카드 개수", example = "10")
	})
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {"USER_NOT_FOUND"}
	)
	ApiResponse<CursorResponseDto<StudyKpiCardDTO.StudyKpiCardNameDTO>> getKpiCards(
		@RequestParam String jobName,
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	);

	@Operation(summary = "맞춤형 스터디 추천 목록 조회 API", description = "유저의 하위 3개 KPI 카드 중 하나라도 포함하고, 잔여석이 있는 스터디를 추천합니다.")
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회된 스터디 ID", example = "100"),
		@Parameter(name = "size", description = "한 번에 조회할 스터디 개수", example = "10")
	})
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {"USER_NOT_FOUND"}
	)
	ApiResponse<CursorResponseDto<StudyRecommendDTO>> getRecommendedStudies(
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size,
		@AuthUser Long userId
	);

	@Operation(summary = "스터디 신청하기 API", description = "사용자가 특정 스터디에 참여 신청을 합니다.")
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {
			"STUDY_NOT_FOUND",
			"USER_NOT_FOUND",
			"STUDY_ALREADY_APPLIED",
			"STUDY_MEMBER_FULL"
		}
	)
	ApiResponse<String> applyStudy(@PathVariable Long studyId, @AuthUser Long userId);

	@Operation(summary = "스터디 신청 현황 목록 조회 API", description = "스터디장이 해당 스터디의 신청자 목록을 조회합니다.")
	@Parameters({
		@Parameter(name = "cursor", description = "마지막으로 조회된 스터디 ID", example = "100"),
		@Parameter(name = "size", description = "한 번에 조회할 스터디 개수", example = "10")
	})
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {
			"STUDY_NOT_FOUND",
			"NOT_STUDY_LEADER"
		}
	)
	ApiResponse<CursorResponseDto<StudyApplicationDTO.ApplicationPreviewDTO>> getApplicants(
		@PathVariable Long studyId,
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	);

	@Operation(summary = "신청 수락/거절 처리 API", description = "스터디장이 신청자의 참여 여부를 결정합니다.")
	@ApiErrorCodes(
		enumClass = StudyErrorCode.class,
		includes = {
			"STUDY_USER_NOT_FOUND",
			"NOT_STUDY_LEADER",
			"INVALID_ATTEND_STATUS"
		}
	)
	ApiResponse<String> processApply(
		@PathVariable Long studyUserId,
		@RequestBody @Valid StudyApplicationDTO.ProcessApplicationDTO request
	);
}