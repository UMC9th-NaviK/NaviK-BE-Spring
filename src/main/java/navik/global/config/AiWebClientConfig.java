package navik.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
@Slf4j
public class AiWebClientConfig {

	@Bean
	public WebClient aiWebClient(AiServerProperties props) {
		return buildWebClient(props.aiBaseUrl(), "AI");
	}

	@Bean
	public WebClient ocrWebClient(AiServerProperties props) {
		return buildWebClient(props.ocrBaseUrl(), "OCR");
	}

	private WebClient buildWebClient(String baseUrl, String tag) {
		WebClient.Builder builder = WebClient.builder();

		if (baseUrl != null && !baseUrl.isBlank()) {
			builder.baseUrl(baseUrl);
		}

		return builder
			.filter((req, next) -> {
				log.info("[{}-REQ] {} {}", tag, req.method(), req.url());
				return next.exchange(req)
					.doOnNext(res ->
						log.info("[{}-RES] status={}", tag, res.statusCode())
					);
			})
			.build();
	}
}
