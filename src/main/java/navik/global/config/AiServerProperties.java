package navik.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "ai.server")
public record AiServerProperties(
	@NotBlank(message = "AI_SERVER_BASE_URL 환경 변수를 설정해주세요")
	String aiBaseUrl,

	@NotBlank(message = "AI_SERVER_OCR_BASE_URL 환경 변수를 설정해주세요")
	String ocrBaseUrl
) {
}