package navik.global.s3.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Amazon S3 관련 데이터 전송 객체(DTO)를 포함하는 클래스입니다.
 */
public class S3Dto {

	/**
	 * Presigned URL 응답을 위한 DTO 클래스입니다.
	 */
	@Getter
	@Builder
	public static class PreSignedUrlResponse {

		/**
		 * AWS S3에 파일을 업로드하기 위해 미리 서명된 URL입니다.
		 * 이 URL에는 업로드에 필요한 모든 권한과 정보가 포함되어 있습니다.
		 */
		private String preSignedUrl;

		/**
		 * S3 버킷 내에서 업로드된 파일의 고유한 경로와 파일명을 나타내는 키입니다.
		 * 이 키는 파일을 식별하고 접근하는 데 사용됩니다.
		 */
		private String key;
	}
}
