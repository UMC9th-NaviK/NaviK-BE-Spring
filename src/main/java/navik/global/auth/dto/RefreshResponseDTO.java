package navik.global.auth.dto;

public record RefreshResponseDTO(
	String accessToken,
	String status
) {
}
