package navik.global.ocr.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

public class NaverOcrRequestDto {

	@Getter
	@Builder
	public static class JsonRequest {
		@Builder.Default
		private String version = "V2";    // V2 엔진 권장
		private String requestId;    // 요청 식별을 위한 UUID
		private long timestamp;    // 요청 시각
		@Builder.Default
		private String lang = "ko";    // 한국어 이미지
		private List<Image> image;    // 호출 당 1개의 이미지 OCR
	}

	@Getter
	@Builder
	public static class Image {
		private String format;    // jpg, jpeg, png, pdf, tif, tiff
		private String name;    // 이미지 식별 이름
		private String url;    // 공개된 이미지 URL
		private String data;   // 또는 Base64 인코딩 데이터 (우선순위)
	}
}
