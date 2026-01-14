package navik.global.s3.service;

import navik.global.s3.dto.S3Dto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

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
	 *
	 * @param prefix   S3 버킷 내에서 파일이 저장될 폴더 경로입니다.
	 * @param fileName 업로드할 파일의 원래 이름입니다.
	 * @return Presigned URL과 파일 키를 포함하는 {@link S3Dto.PreSignedUrlResponse} 객체를
	 * 반환합니다.
	 */
	public S3Dto.PreSignedUrlResponse getPreSignedUrl(String prefix, String fileName) {
		String key = prefix + "/" + UUID.randomUUID() + "_" + fileName;

		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.putObjectRequest(objectRequest)
			.build();

		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

		return S3Dto.PreSignedUrlResponse.builder()
			.preSignedUrl(presignedRequest.url().toString())
			.key(key)
			.build();
	}
}
