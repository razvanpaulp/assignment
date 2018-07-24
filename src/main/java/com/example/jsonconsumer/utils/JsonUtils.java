package com.example.jsonconsumer.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}