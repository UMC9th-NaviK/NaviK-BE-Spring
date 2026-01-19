package navik.domain.portfolio.dto;

import jakarta.validation.constraints.NotNull;
import navik.domain.portfolio.entity.InputType;

public class PortfolioRequestDto {

	public record Create(
		@NotNull(message = "입력 타입은 필수입니다.")
		InputType inputType,
		String content,
		String fileUrl
	) {
	}
}
