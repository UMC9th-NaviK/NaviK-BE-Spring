package navik.global.auth.dto;

public record RefreshDTO(
	TokenDTO tokenDTO,
	String status) {
}
