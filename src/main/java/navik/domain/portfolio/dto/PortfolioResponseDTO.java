package navik.domain.portfolio.dto;

import navik.domain.portfolio.entity.InputType;

public class PortfolioResponseDTO {

	public record Created(
		Long id,
		InputType inputType
	) {
	}

	public record AdditionalInfoSubmitted(
		Long portfolioId
	) {
	}
}
