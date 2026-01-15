package navik.domain.recruitment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import navik.domain.recruitment.dto.RecruitmentResponseDTO;
import navik.domain.recruitment.service.RecruitmentQueryService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.auth.annotation.AuthUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recruitments")
public class RecruitmentController implements RecruitmentControllerDocs {

	private final RecruitmentQueryService recruitmentQueryService;

	@GetMapping("/recommend")
	public ApiResponse<List<RecruitmentResponseDTO.RecommendPost>> getRecommendedPost(@AuthUser Long userId) {
		List<RecruitmentResponseDTO.RecommendPost> result = recruitmentQueryService.getRecommendedPost(userId);
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);
	}
}
