package navik.domain.study.controller;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.dto.StudyKpiCardDTO;
import navik.domain.study.enums.StudyRole;
import navik.global.apiPayload.ApiResponse;
import navik.global.dto.CursorResponseDTO;

@Tag(name = "Study", description = "스터디 관련 API")
public interface StudyControllerDocs {
	@Operation(summary = "스터디 생성 API", description = "새로운 스터디를 생성합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
		})
	ApiResponse<Long> createStudy(StudyCreateDTO.CreateDTO request, Long userId);

	@Operation(summary = "나의 스터디 목록 조회 API", description = "커서 기반 페이징으로 나의 스터디를 조회합니다.")
	@Parameters({
		@Parameter(name = "role", description = "필터링할 역할 (STUDY_LEADER: 내가 만든 스터디, STUDY_MEMBER: 내가 참여한 스터디)", example = "STUDY_LEADER"),
		@Parameter(name = "cursor", description = "마지막으로 조회된 스터디 유저 ID (StudyUser의 PK)", example = "50"),
		@Parameter(name = "size", description = "한 번에 조회할 스터디 개수", example = "10")
	})
	ApiResponse<CursorResponseDTO<StudyDTO.MyStudyDTO>> getMyStudies(StudyRole role, Long cursor, int size,
		Long userId);

	@Operation(summary = "직무별 KPI 카드 목록 조회 API", description = "스터디 생성 시 특정 직무를 선택했을 때 해당되는 KPI 카드 리스트를 조회합니다. 커서 기반 페이징(무한 스크롤)을 지원합니다.")
	@Parameters({
		@Parameter(name = "jobName", description = "조회할 직무의 이름 (예: 기획, 개발, 디자인 등)", example = "개발"),
		@Parameter(name = "cursor", description = "마지막으로 조회된 KPI 카드의 ID.", example = "10"),
		@Parameter(name = "size", description = "한 번에 조회할 카드 개수", example = "10")
	})
	ApiResponse<CursorResponseDTO<StudyKpiCardDTO.StudyKpiCardNameDTO>> getKpiCards(
		@RequestParam String jobName,
		@RequestParam(value = "cursor", required = false) Long cursor,
		@RequestParam(value = "size", defaultValue = "10") int size
	);
}