package navik.domain.growthLog.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
public class AiWebClientConfig {

	@Bean
	public WebClient aiWebClient(AiServerProperties props) {
		WebClient.Builder builder = WebClient.builder();

		if (props.aiBaseUrl() != null && !props.aiBaseUrl().isBlank()) {
			builder.baseUrl(props.aiBaseUrl());
		}

		return builder.build();
	}

	@Bean
	public WebClient ocrWebClient(AiServerProperties props) {
		WebClient.Builder builder = WebClient.builder();

		if (props.ocrBaseUrl() != null && !props.ocrBaseUrl().isBlank()) {
			builder.baseUrl(props.ocrBaseUrl());
		}

		return builder.build();
	}
}
