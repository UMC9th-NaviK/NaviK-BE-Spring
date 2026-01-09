package navik.global.auth.service;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import navik.domain.users.entity.User;
import navik.domain.users.enums.Role;

@Getter
public class OAuthAttributes {
	private final Map<String, Object> attributes;
	private final String nameAttributeKey;
	private final String name;
	private final String email;
	private final String socialType;
	private final String socialId;

	@Builder
	public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
		String socialType, String socialId) {
		this.attributes = attributes;
		this.nameAttributeKey = nameAttributeKey;
		this.name = name;
		this.email = email;
		this.socialType = socialType;
		this.socialId = socialId;
	}

	public static OAuthAttributes of(String registrationId, String userNameAttributeName,
		Map<String, Object> attributes) {

		return switch (registrationId) {
			case "google" -> ofGoogle(userNameAttributeName, attributes);
			case "naver" -> ofNaver(userNameAttributeName, attributes);
			case "kakao" -> ofKakao(userNameAttributeName, attributes);
			default -> null;
		};
	}

	private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
		return OAuthAttributes.builder()
			.name((String)attributes.get("name"))
			.email((String)attributes.get("email"))
			.socialType("google")
			.socialId((String)attributes.get("sub"))
			.attributes(attributes)
			.nameAttributeKey(userNameAttributeName)
			.build();
	}

	private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");

		return OAuthAttributes.builder()
			.name((String)response.get("name"))
			.email((String)response.get("email"))
			.socialType("naver")
			.socialId((String)response.get("id"))
			.attributes(response)
			.nameAttributeKey(userNameAttributeName)
			.build();
	}

	public static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");

		return OAuthAttributes.builder()
			.name((String)profile.get("nickname")) // todo: 카카오 디벨로퍼스 이름 권한 설정 ?
			.email((String)kakaoAccount.get("email"))
			.socialType("kakao")
			.socialId(String.valueOf(attributes.get("id")))
			.attributes(attributes)
			.nameAttributeKey(userNameAttributeName)
			.build();
	}

	public User toEntity() {
		return User.builder()
			.name(name)
			.email(email)
			.role(Role.USER)
			.socialType(socialType)
			.socialId(socialId)
			.build();
	}
}
