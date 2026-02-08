package navik.domain.portfolio.dto;

import navik.domain.portfolio.entity.InputType;
import navik.domain.portfolio.entity.PortfolioStatus;

public class PortfolioResponseDTO {

	public record Created(Long id, InputType inputType, PortfolioStatus status

	) {
	}

	public record AdditionalInfoSubmitted(Long portfolioId) {
	}

	public record Status(Long portfolioId, PortfolioStatus status) {
	}
}
