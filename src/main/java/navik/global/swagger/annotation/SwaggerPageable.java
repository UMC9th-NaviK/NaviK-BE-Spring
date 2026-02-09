package navik.global.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
	@Parameter(
		in = ParameterIn.QUERY,
		name = "page",
		description = "페이지 번호 (0부터 시작)",
		schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", defaultValue = "0")
	),
	@Parameter(
		in = ParameterIn.QUERY,
		name = "size",
		description = "한 페이지에 표시될 데이터 개수",
		schema = @io.swagger.v3.oas.annotations.media.Schema(type = "integer", defaultValue = "10")
	),
	@Parameter(
		in = ParameterIn.QUERY,
		name = "sort",
		description = "정렬 조건",
		array = @ArraySchema(schema = @Schema(type = "string"))
	),
	@Parameter(
		name = "pageable",
		hidden = true
	)
})
public @interface SwaggerPageable {
}
