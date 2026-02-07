package navik.global.auth.handler;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import navik.global.apiPayload.exception.code.AuthErrorCode;
import navik.global.apiPayload.exception.exception.GeneralException;
import navik.global.auth.JwtUserDetails;
import navik.global.auth.annotation.AuthUser;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthUser.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 인증 정보가 없거나 익명 사용자인 경우 예외 발생
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
			|| !authentication.isAuthenticated()) {
			throw new GeneralException(AuthErrorCode.UNAUTHORIZED);
		}

		Object principal = authentication.getPrincipal();

		// Principal이 UserDetails 타입인지 확인
		if (!(principal instanceof UserDetails)) {
			throw new GeneralException(AuthErrorCode.UNAUTHORIZED);
		}

		return ((JwtUserDetails)principal).getUserId();
	}
}
