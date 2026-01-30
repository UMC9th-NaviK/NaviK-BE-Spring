package navik.global.s3.controller;

import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import navik.global.s3.S3PathType;
import navik.global.s3.dto.S3Dto;
import navik.global.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/s3")
@Tag(name = "S3", description = "S3 관련 API")
@ConditionalOnProperty(name = "spring.cloud.aws.s3.enabled", havingValue = "true")
public class S3Controller {

	private final S3Service s3Service;

	@GetMapping("/presigned-url")
	@Operation(summary = "Presigned URL 생성", description = "S3 업로드를 위한 Presigned URL을 생성합니다.(key = ImageUrl)")
	public ApiResponse<S3Dto.PreSignedUrlResponse> getPresignedUrl(
		@Parameter(description = "S3 경로 타입", example = "PORTFOLIO_PDF") @RequestParam S3PathType pathType,
		@Parameter(description = "경로에 사용될 ID (userId, boardId 등)", example = "1") @RequestParam Long id,
		@Parameter(description = "파일 확장자", example = ".pdf") @RequestParam String extension) {

		return ApiResponse.onSuccess(GeneralSuccessCode._OK, s3Service.getPreSignedUrl(pathType, id, extension));
	}
}
