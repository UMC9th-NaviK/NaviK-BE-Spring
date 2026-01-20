package navik.domain.study.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.study.dto.StudyCreateDTO;
import navik.domain.study.dto.StudyDTO;
import navik.domain.study.enums.StudyRole;
import navik.global.apiPayload.ApiResponse;
import navik.global.dto.CursorResponseDto;

@Tag(name = "Study", description = "스터디 관련 API")
public interface StudyControllerDocs {
	@Operation(summary = "스터디 생성 API", description = "새로운 스터디를 생성합니다.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(
		value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CREATED")
		})
	ApiResponse<Long> createStudy(StudyCreateDTO.CreateDTO request, Long userId);

	@Operation(summary = "나의 스터디 목록 조회 API", description = "커서 기반 페이징으로 나의 스터디를 조회합니다.")
	ApiResponse<CursorResponseDto<StudyDTO.MyStudyDTO>> getMyStudies(StudyRole role, Long cursor, int size,
		Long userId);
}