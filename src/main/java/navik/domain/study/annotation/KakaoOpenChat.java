package navik.domain.study.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import navik.domain.study.validator.KakaoOpenChatValidator;

@Documented
@Constraint(validatedBy = KakaoOpenChatValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KakaoOpenChat {
	String message() default "올바른 카카오 오픈채팅 링크가 아닙니다. https://open.kakao.com/o/XXXXXXX";

	Class<?>[] groups() default {};

	Class<? extends jakarta.validation.Payload>[] payload() default {};
}
