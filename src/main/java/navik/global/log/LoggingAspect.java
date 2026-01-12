package navik.global.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션의 로깅을 담당하는 Aspect 클래스입니다.
 * 컨트롤러와 서비스 레이어의 메서드 실행에 대한 로그를 기록합니다.
 */
@Slf4j
@Aspect
@Component
@Profile(value = "dev")
public class LoggingAspect {

	/**
	 * 컨트롤러와 서비스 레이어 전체를 대상으로 하는 Pointcut을 정의합니다.
	 */
	@Pointcut("execution(* navik.domain..controller..*(..)) || execution(* navik.domain..service..*(..))")
	public void applicationLayer() {
	}

	/**
	 * 메서드 실행 전에 요청 정보를 로그로 기록합니다.
	 *
	 * @param joinPoint 프록시된 메서드에 대한 정보를 제공하는 JoinPoint 객체
	 */
	@Before("applicationLayer()")
	public void logRequest(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().toShortString();
		Object[] args = joinPoint.getArgs();
		log.info("▶️요청 - {} | args = {}", methodName, args);
	}

	/**
	 * 메서드가 정상적으로 실행되고 반환된 후에 응답 정보를 로그로 기록합니다.
	 *
	 * @param joinPoint 프록시된 메서드에 대한 정보를 제공하는 JoinPoint 객체
	 * @param result    메서드가 반환한 결과 객체
	 */
	@AfterReturning(pointcut = "applicationLayer()", returning = "result")
	public void logResponse(JoinPoint joinPoint, Object result) {
		String methodName = joinPoint.getSignature().toShortString();
		log.info("✅응답 - {} | result = {}", methodName, result);
	}

	/**
	 * 메서드 실행 중 예외가 발생했을 때 예외 정보를 로그로 기록합니다.
	 *
	 * @param joinPoint 프록시된 메서드에 대한 정보를 제공하는 JoinPoint 객체
	 * @param e         발생한 예외 객체
	 */
	@AfterThrowing(pointcut = "applicationLayer()", throwing = "e")
	public void logException(JoinPoint joinPoint, Throwable e) {
		String methodName = joinPoint.getSignature().toShortString();
		log.error("❌예외 - {} | message = {}", methodName, e.getMessage(), e);
	}

	/**
	 * 메서드의 실행 시간을 측정하고 로그로 기록합니다.
	 *
	 * @param joinPoint 프록시된 메서드에 대한 정보를 제공하는 ProceedingJoinPoint 객체
	 * @return 실제 메서드가 반환하는 결과 객체
	 * @throws Throwable 메서드 실행 중 발생할 수 있는 예외
	 */
	@Around("applicationLayer()")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();

		Object result = joinPoint.proceed();  // 실제 메서드 실행

		long end = System.currentTimeMillis();
		String methodName = joinPoint.getSignature().toShortString();
		log.info("⏱️실행 시간 - {} | {} ms", methodName, (end - start));

		return result;
	}
}
