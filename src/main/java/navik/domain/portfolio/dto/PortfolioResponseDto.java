package navik.domain.portfolio.dto;

import navik.domain.portfolio.entity.InputType;

public class PortfolioResponseDto {

	public record Created(
		Long id,
		InputType inputType
	) {
	}
}
