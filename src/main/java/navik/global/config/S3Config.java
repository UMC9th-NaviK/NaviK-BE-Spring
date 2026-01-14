package navik.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Amazon S3(Simple Storage Service) 관련 설정을 담당하는 클래스입니다.
 * AWS SDK for Java v2를 사용하여 S3 Presigner를 설정합니다.
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.aws.s3.enabled", havingValue = "true")
public class S3Config {

	@Value("${spring.cloud.aws.region.static}")
	private String region;

	/**
	 * S3 Presigner를 Spring Bean으로 생성하여 제공합니다.
	 * S3 Presigner는 S3 객체에 대한 미리 서명된 URL을 생성하는 데 사용됩니다.
	 *
	 * @return S3Presigner 인스턴스
	 */
	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.region(Region.of(region))
			.credentialsProvider(DefaultCredentialsProvider.create())
			.build();
	}
}
