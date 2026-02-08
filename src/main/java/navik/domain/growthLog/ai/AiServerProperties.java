package navik.domain.growthLog.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "ai.server")
public record AiServerProperties(@NotBlank(message = "AI_SERVER_BASE_URL 환경 변수를 설정해주세요")
								 String aiBaseUrl,
								 String ocrBaseUrl) {
}