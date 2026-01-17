package navik.domain.term.dto;

import java.util.List;

public class TermRequestDTO {

	public record AgreeDTO(List<Long> termIds) {
	}
}
