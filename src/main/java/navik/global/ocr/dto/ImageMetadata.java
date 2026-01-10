package navik.global.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ImageMetadata {
	private final String format;
	private final int width;
	private final int height;
	private final long fileSize;
}
