package navik.global.ocr.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.global.ocr.constant.NaverOcrConstant;
import navik.global.ocr.dto.ImageMetadataDTO;
import navik.global.ocr.dto.NaverOcrRequestDTO;
import navik.global.ocr.dto.NaverOcrResponseDTO;

/**
 * 구글 OCR 쓰려고 했으나, 결제 인증 문제로 인해 네이버 OCR로 임시 대체하였습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOcrService implements OcrService {

	@Value("${naver.service.url}")
	private String apiUrl;
	@Value("${naver.service.secretKey}")
	private String secretKey;
	private final ImageHelper imageHelper;
	private final WebClient webClient;

	/**
	 * 네이버 권장 사항
	 * 	  - 최대 용량: 50MB
	 *    - 지원 확장자: "jpg", "jpeg", "png", "pdf", "tif", "tiff"
	 *
	 * @param imageUrl
	 * @return
	 */
	@Override
	public String extractFromImageUrl(String imageUrl) {

		// 1. 이미지 메타데이터 추출
		ImageMetadataDTO metadata = imageHelper.getMetadata(imageUrl);
		if (metadata == null) {
			log.warn("[NaverOcrService] 이미지 메타데이터 추출에 실패하였습니다.");
			return "";
		}

		// 2. 이미지 최대 용량 검사
		long size = metadata.getFileSize();
		if (!isSupportedFileSize(size)) {
			log.warn("[NaverOcrService] 최대 용량을 초과하였습니다.");
			return "";
		}

		// 3. API 호출 비용 절감을 위한 작은 이미지 제외
		int width = metadata.getWidth();
		int height = metadata.getHeight();
		if (isTrashImage(width, height)) {
			log.info("[NaverOcrService] 매우 작은 이미지입니다.");
			return "";
		}

		// 4. 이미지 확장자 검사
		String extension = metadata.getExtension();
		if (!isSupportedExtension(extension)) {
			log.warn("[NaverOcrService] 미지원 확장자입니다.");
			return "";
		}

		// 5. API 호출 결과 반환
		return requestApi(imageUrl, extension);
	}

	private String requestApi(String imageUrl, String extension) {

		// 1. inner DTO 생성
		NaverOcrRequestDTO.Image image = NaverOcrRequestDTO.Image.builder()
			.format(extension)
			.name("navik-image")
			.url(imageUrl)
			.build();

		// 2. inner DTO 포함, 최종 request body 생성
		NaverOcrRequestDTO.OcrRequest requestBody = NaverOcrRequestDTO.OcrRequest.builder()
			.version(NaverOcrConstant.RECOMMENDED_VERSION)
			.requestId(UUID.randomUUID().toString())
			.timestamp(System.currentTimeMillis())
			.lang(NaverOcrConstant.LANG_KOREAN)
			.images(List.of(image))
			.build();

		// 3. Naver OCR API 호출
		NaverOcrResponseDTO.OcrResponse responseBody = webClient.post()
			.uri(apiUrl)
			.header("X-OCR-SECRET", secretKey)
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(requestBody))
			.retrieve()
			.bodyToMono(NaverOcrResponseDTO.OcrResponse.class)
			.block();

		// 4. 텍스트 추출
		String result = responseBody.getImages().stream()
			.filter(img -> "SUCCESS".equals(img.getInferResult()))
			.flatMap(img -> img.getFields().stream())
			.map(NaverOcrResponseDTO.Field::getInferText)
			.collect(Collectors.joining(" "));

		// 5. OCR 결과 반환
		return result.toString().trim();
	}

	private boolean isSupportedExtension(String extension) {
		return NaverOcrConstant.SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
	}

	private boolean isSupportedFileSize(long size) {
		return size > 0 && size <= NaverOcrConstant.MAX_FILE_SIZE;
	}

	private boolean isTrashImage(int width, int height) {
		return width <= NaverOcrConstant.TRASH_PIXEL_SIZE || height <= NaverOcrConstant.TRASH_PIXEL_SIZE;
	}
}
