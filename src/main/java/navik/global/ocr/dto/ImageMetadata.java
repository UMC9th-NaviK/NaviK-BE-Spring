package navik.global.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ImageMetadata {
	private final String extension; // 확장자
	private final int width;        // 가로 길이
	private final int height;       // 세로 길이
	private final long fileSize;    // 용량
}
