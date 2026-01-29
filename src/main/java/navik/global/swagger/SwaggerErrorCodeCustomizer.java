package navik.global.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.code.status.BaseCode;

@Slf4j
@Configuration
public class SwaggerErrorCodeCustomizer {

	@Bean
	public OperationCustomizer apiErrorCodesCustomizer() {
		return (operation, handlerMethod) -> {

			List<ApiErrorCodes> annotations = new ArrayList<>();

			ApiErrorCodes single = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
			ApiErrorCodesGroup group = handlerMethod.getMethodAnnotation(ApiErrorCodesGroup.class);

			if (single != null)
				annotations.add(single);
			if (group != null)
				annotations.addAll(Arrays.asList(group.value()));

			for (ApiErrorCodes ann : annotations) {
				for (String name : ann.includes()) {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends Enum> enumClass = (Class<? extends Enum>)ann.enumClass();
						Enum<?> constant = Enum.valueOf(enumClass, name);

						if (constant instanceof BaseCode baseCode) {
							addErrorResponse(operation, baseCode);
						}
					} catch (IllegalArgumentException e) {
						System.err.println("Invalid enum: " + name);
					}
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

		ApiResponse existing = operation.getResponses().get(statusCode);
		if (existing != null) {
			existing.getContent()
				.get("application/json")
				.addExamples(baseCode.getCode(), example);
		} else {
			operation.getResponses().addApiResponse(statusCode, apiResponse);
		}
	}
}


