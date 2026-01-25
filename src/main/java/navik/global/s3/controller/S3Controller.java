package navik.global.s3.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.code.GeneralSuccessCode;
import navik.global.s3.dto.S3DTO;
import navik.global.s3.service.S3Service;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/s3")
@Tag(name = "S3", description = "S3 관련 API")
@ConditionalOnProperty(name = "spring.cloud.aws.s3.enabled", havingValue = "true")
public class S3Controller {

	private final S3Service s3Service;

	/**
	 * S3 업로드를 위한 Presigned URL을 생성합니다.
	 *
	 * @param prefix   저장할 폴더 경로 (예: user/profile)
	 * @param fileName 업로드할 파일의 이름 (확장자 포함)
	 * @return Presigned URL 응답
	 */
	@GetMapping("/presigned-url")
	@Operation(summary = "Presigned URL 생성", description = "S3 업로드를 위한 Presigned URL을 생성합니다.")
	public ApiResponse<S3DTO.PreSignedUrlResponse> getPresignedUrl(
		@Parameter(description = "저장할 폴더 경로 (예: user/profile)", example = "test-folder") @RequestParam String prefix,
		@Parameter(description = "업로드할 파일의 이름 (확장자 포함)", example = "image.png") @RequestParam String fileName) {
		return ApiResponse.onSuccess(GeneralSuccessCode._OK, s3Service.getPreSignedUrl(prefix, fileName));
	}
}
