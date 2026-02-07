package navik.global.swagger;

import java.util.LinkedHashMap;
import java.util.Map;

import navik.global.apiPayload.exception.code.BaseCode;

public class SwaggerErrorExampleFactory {

	private SwaggerErrorExampleFactory() {
	}

	public static Map<String, Object> from(BaseCode code) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("isSuccess", false);
		m.put("code", code.getCode());
		m.put("message", code.getMessage());
		m.put("result", null);
		m.put("timestamp", "2025-01-01T00:00:00");

		return m;
	}
}
