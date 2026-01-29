package navik.global.swagger;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import navik.global.apiPayload.code.status.BaseCode;

@Configuration
public class SwaggerErrorCodeCustomizer {

	@Bean
	public OperationCustomizer apiErrorCodesCustomizer() {
		return (operation, handlerMethod) -> {

			ApiErrorCodes ann = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
			if (ann == null)
				return operation;

			Class<? extends Enum<?>> enumClass = ann.enumClass();
			String[] includes = ann.includes();

			for (String name : includes) {
				Enum<?> constant = Enum.valueOf(enumClass.asSubclass(Enum.class), name);

				if (constant instanceof BaseCode baseCode) {
					addErrorResponse(operation, baseCode);
				}
			}

			return operation;
		};
	}

	private void addErrorResponse(
		io.swagger.v3.oas.models.Operation operation,
		BaseCode baseCode
	) {
		String statusCode = String.valueOf(baseCode.getHttpStatus().value());

		Example example = new Example()
			.summary(baseCode.getCode())
			.value(SwaggerErrorExampleFactory.from(baseCode));

		MediaType mediaType = new MediaType()
			.addExamples(baseCode.getCode(), example);

		ApiResponse apiResponse = new ApiResponse()
			.description(baseCode.getMessage())
			.content(new Content().addMediaType("application/json", mediaType));

		operation.getResponses().addApiResponse(statusCode, apiResponse);
	}
}
