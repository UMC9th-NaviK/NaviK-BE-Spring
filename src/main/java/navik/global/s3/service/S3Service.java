package navik.global.s3.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.global.apiPayload.exception.code.GeneralErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.s3.dto.S3DTO;
import navik.global.s3.enums.S3PathType;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * Amazon S3 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.aws.s3.enabled", havingValue = "true")
public class S3Service {

	private final S3Presigner s3Presigner;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	/**
	 * S3에 파일을 업로드하기 위한 Presigned URL을 생성합니다.
	 * S3PathType에 정의된 경로 및 파일명 규칙에 따라 key를 생성합니다.
	 *
	 * @param pathType  S3 경로 타입 (PORTFOLIO_PDF, BOARD_IMAGE 등)
	 * @param id        경로에 사용될 ID (userId, boardId 등)
	 * @param extension 파일 확장자 (.pdf, .png 등)
	 * @return Presigned URL과 파일 키를 포함하는 응답 객체
	 */
	public S3DTO.PreSignedUrlResponse getPreSignedUrl(S3PathType pathType, Long id, String extension) {

		if (id == null || extension == null) {
			throw new GeneralException(GeneralErrorCode.INVALID_INPUT_VALUE);
		}

		String key = pathType.generateKey(id, extension);

		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.putObjectRequest(objectRequest)
			.build();

		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

		return S3DTO.PreSignedUrlResponse.builder()
			.preSignedUrl(presignedRequest.url().toString())
			.key(key)
			.build();
	}
}
