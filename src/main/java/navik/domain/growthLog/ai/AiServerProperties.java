package navik.domain.growthLog.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.server")
public record AiServerProperties(
	String baseUrl
) {
}