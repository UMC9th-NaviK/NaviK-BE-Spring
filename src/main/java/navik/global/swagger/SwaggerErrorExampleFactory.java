package navik.global.swagger;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import navik.global.apiPayload.code.status.BaseCode;

public class SwaggerErrorExampleFactory {

	private SwaggerErrorExampleFactory() {
	}

	public static Map<String, Object> from(BaseCode code) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("isSuccess", false);
		m.put("code", code.getCode());
		m.put("message", code.getMessage());
		m.put("result", null);
		m.put("timestamp", LocalDateTime.now().withNano(0).toString());
		return m;
	}
}
