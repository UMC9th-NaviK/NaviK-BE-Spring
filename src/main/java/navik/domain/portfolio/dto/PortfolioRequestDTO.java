package navik.domain.portfolio.dto;

import jakarta.validation.constraints.NotNull;
import navik.domain.portfolio.entity.InputType;

public class PortfolioRequestDTO {

	public record Create(
		@NotNull(message = "입력 타입은 필수입니다.")
		InputType inputType,
		String content,
		String fileUrl
	) {
	}

	public record AdditionalInfo(
		@NotNull(message = "qB1은 필수입니다.")
		Integer qB1,
		@NotNull(message = "qB2은 필수입니다.")
		Integer qB2,
		@NotNull(message = "qB3은 필수입니다.")
		Integer qB3,
		@NotNull(message = "qB4은 필수입니다.")
		Integer qB4,
		@NotNull(message = "qB5은 필수입니다.")
		Integer qB5
	) {
	}
}
