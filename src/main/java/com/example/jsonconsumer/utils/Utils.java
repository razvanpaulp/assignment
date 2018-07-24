package com.example.jsonconsumer.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class Utils {
	public List<String> splitByDelimiter(String input, String delimiter) {

		return Collections.list(new StringTokenizer(input.trim(), delimiter)).stream().map(token -> (String) token)
				.collect(Collectors.toList());
	}

	public boolean containsPattern(String endpoint, String patternToMatch) {
		Pattern pattern = Pattern.compile(patternToMatch);
		Matcher matcher = pattern.matcher(endpoint);
		return matcher.find();
	}

	public List<String> findVariables(String endpoint) {

		Pattern pattern = Pattern.compile("\\{.*?\\}");

		Matcher matcher = pattern.matcher(endpoint);

		List<String> resultList = new ArrayList<String>();
		while (matcher.find()) {
			String match = matcher.group(0).replaceAll(("\\{|\\}"), "");
			String[] jsonVariableArray = match.split(",");
			for (String jsonVariable : jsonVariableArray) {
				resultList.add(jsonVariable);
			}
		
		}
		return resultList;

	}
	
	public boolean checkValidity(String response) {
		Pattern pattern = Pattern.compile("\\[\\]");
		Matcher matcher = pattern.matcher(response);
		return !matcher.matches();
	}
	
	public static final boolean isUTF8(final byte[] inputBytes) {
	    final String converted = new String(inputBytes, StandardCharsets.UTF_8);
	    final byte[] outputBytes = converted.getBytes(StandardCharsets.UTF_8);
	    return Arrays.equals(inputBytes, outputBytes);
	}
}
