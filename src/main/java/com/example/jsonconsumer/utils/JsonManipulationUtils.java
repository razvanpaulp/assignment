package com.example.jsonconsumer.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.jsonconsumer.exception.ParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonManipulationUtils {
	@Value("${client-id}")
	public String clientId;

	@Value("${client-secret}")
	public String clientSecret;

	@Autowired
	Utils regexUtils;


	public String replaceEndpointVariables(String endpoint, List<String> endpointVariables, Map<String, String> jsonVariables) throws IOException, ParseException {

		final Map<String, JsonNode> finalResult = new HashMap<>();

		for (final String variable : endpointVariables) {
			final List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");
			final String json = jsonVariables.get(splitVariables.get(0));
			if (json.matches("\\{\".*")) {
				JsonNode node  = this.getJsonObject(variable, json);
				if (node == null) { 
					throw new ParseException("Empty json array");
				}
				finalResult.put(variable, node);
			}
		}

		for (int i = 0; i < endpointVariables.size(); i++) {
			String valueToReplaceWith = finalResult.get(endpointVariables.get(i)).toString();
			valueToReplaceWith = URLEncoder.encode(valueToReplaceWith.replace("\"", ""), "UTF-8");
			endpoint = endpoint.replace(endpointVariables.get(i), valueToReplaceWith);

		}

		endpoint = endpoint.replaceAll("\\b(\\w*CLIENT_ID\\w*)\\b", clientId)
		.replaceAll("\\b(\\w*CLIENT_SECRET\\w*)\\b", clientSecret).replaceAll("\\}|\\{|\\\"", "")
		.replaceAll(" ", "+");
		return endpoint;
	}

	public JsonNode getJsonObject(String variable, String json) throws IOException {
		List<String> splitVariables = regexUtils.splitByDelimiter(variable, ".");

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);
			for (String v : splitVariables.stream().skip(1).collect(Collectors.toList())) {
					if (regexUtils.containsPattern(v, "\\[.*\\]")) {

						rootNode = rootNode.path(v.replaceAll("\\[.*\\]", ""));
						if (rootNode.size() > 0) {
							rootNode = rootNode.get(Integer.parseInt(v.substring(v.indexOf("[") + 1, v.indexOf("]"))));
						} else {
							break;
						}
					} else {
						rootNode = rootNode.path(v);
					}
			}
			if (regexUtils.checkValidity(rootNode.toString())) {
				return rootNode;
			}

		return null;
	}

}
