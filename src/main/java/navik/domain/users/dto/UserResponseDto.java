package navik.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import navik.domain.users.entity.User;
import navik.domain.users.enums.Role;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

	private Long id;
	private String name;
	private String email;
	private Role role;
	private String socialType;

	public static UserResponseDto from(User user) {
		return UserResponseDto.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail())
			.role(user.getRole())
			.socialType(user.getSocialType())
			.build();
	}
}
