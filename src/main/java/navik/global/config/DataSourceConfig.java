package navik.global.config;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("prod")
public class DataSourceConfig {

	@Bean
	public static BeanPostProcessor dataSourceBeanPostProcessor(MeterRegistry registry) {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (bean instanceof DataSource) {
					return ProxyDataSourceBuilder.create((DataSource)bean)
						.name("MyDS-Proxy")
						.afterQuery((execInfo, queryInfoList) -> {
							String uri = getCurrentUri();
							registry.counter("api.db.query.count", "uri", uri).increment();
						})
						.build();
				}
				return bean;
			}
		};
	}

	private static String getCurrentUri() {
		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				String pattern = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
				return (pattern != null) ? pattern : request.getRequestURI();
			}
		} catch (Exception e) {
			log.error("DataSourceConfig - getCurrentUri() 예외", e);
			return "UNKNOWN";
		}
		return "UNKNOWN";
	}
}
