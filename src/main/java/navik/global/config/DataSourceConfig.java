package navik.global.config;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@Profile("prod")
public class DataSourceConfig {

	@Bean
	public BeanPostProcessor dataSourceBeanPostProcessor(MeterRegistry registry) {
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

	private String getCurrentUri() {
		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				return request.getRequestURI();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return "UNKNOWN";
	}
}
