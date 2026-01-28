package navik.global.auth.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import navik.global.apiPayload.exception.code.AuthErrorCode;
import navik.global.apiPayload.exception.exception.GeneralExceptionHandler;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BlacklistAspect {

	private final StringRedisTemplate redisTemplate;

	@Before("@annotation(navik.global.auth.annotation.CheckBlacklist)")
	public void checkBlacklist() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
			.getRequest();
		String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			String accessToken = bearerToken.substring(7);

			// Redis에 BlackList로 저장되어 있는지 확인
			String isLogout = redisTemplate.opsForValue().get("blacklist:" + accessToken);

			if (StringUtils.hasText(isLogout)) {
				throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
			}
		}
	}
}
