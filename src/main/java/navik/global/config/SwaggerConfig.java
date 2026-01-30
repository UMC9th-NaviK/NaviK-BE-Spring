package navik.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		String jwtSchemeName = "JWT Authentication";

		// API 요청헤더에 인증정보 포함
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

		// SecuritySchemes 등록
		Components components = new Components()
			.addSecuritySchemes(jwtSchemeName, new SecurityScheme()
				.name(jwtSchemeName)
				.type(SecurityScheme.Type.HTTP) // HTTP 방식
				.scheme("bearer")
				.bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자열(Optional)

		return new OpenAPI()
			.info(apiInfo())
			.servers(List.of(new Server().url("/")))
			.addSecurityItem(securityRequirement)
			.components(components);
	}

	private Info apiInfo() {
		return new Info()
			.title("Navik API Documentation")
			.description("Navik API 명세서입니다.")
			.version("1.0.0");
	}
}
