package navik.domain.study.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import navik.domain.study.annotation.KakaoOpenChat;

public class KakaoOpenChatValidator implements ConstraintValidator<KakaoOpenChat, String> {

	private static final Pattern KAKAO_OPEN_CHAT_PATTERN = Pattern.compile(
		"^https:\\/\\/open\\.kakao\\.com\\/o\\/[A-Za-z0-9]+$"
	);

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return false;
		}
		return KAKAO_OPEN_CHAT_PATTERN.matcher(value).matches();
	}
}
