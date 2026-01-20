package navik.domain.study.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import navik.domain.study.dto.StudyCreateDTO;
import navik.global.apiPayload.ApiResponse;

@Tag(name = "Study", description = "스터디 관련 API")
public interface StudyControllerDocs {
	@Operation(summary = "스터디 생성 API", description = "새로운 스터디를 생성합니다.")
	ApiResponse<Long> createStudy(StudyCreateDTO.CreateDTO request, Long userId);
}