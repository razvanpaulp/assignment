package com.example.jsonconsumer.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.jsonconsumer.exception.ParseException;
import com.example.jsonconsumer.utils.JsonManipulationUtils;
import com.example.jsonconsumer.utils.JsonUtils;
import com.example.jsonconsumer.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommandExecutor {


	@Autowired
	Utils regexUtils;
	@Autowired
	JsonManipulationUtils jsonUtils;

	private static final Logger LOG = LoggerFactory.getLogger(CommandExecutor.class);


	public CompletableFuture<String> fetch(Command command, Map<String, String> variables) throws IOException {

		String endpoint = command.getParameter();
		// if endpoint contains variables
		if (regexUtils.containsPattern(endpoint, "\\{.*?\\}")) {
			List<String> endpointVariables = regexUtils.findVariables(endpoint);
			String formattedEndpoint;
			try {
				formattedEndpoint = jsonUtils.replaceEndpointVariables(endpoint, endpointVariables, variables);
				System.out.println( formattedEndpoint);
				return this.fetchPromise(formattedEndpoint);
			} catch (ParseException e) {
				return emptyResult();
			}


		} else {
			return this.fetchPromise(endpoint);
		}
	}

	public CompletableFuture<String> download(Command command, Map<String, String> variables) throws IOException {

		String url = command.getVariableName();
		String localPath = command.getParameter();
		List<String> urlVariables = regexUtils.findVariables(url);
		List<String> pathVariables = regexUtils.findVariables(localPath);
		try {
			String formattedUrl = jsonUtils.replaceEndpointVariables(url, urlVariables, variables);
			String formatedPath = jsonUtils.replaceEndpointVariables(localPath, pathVariables, variables);
			System.out.println( formattedUrl);
			System.out.println( formatedPath);

			return this.fetchDownloadPromise(formattedUrl, formatedPath);
		} catch (ParseException e) {
			return emptyResult();
		}


	}

	public String downloadImage(String url, String path) {
		try {
			String decodedURL = URLDecoder.decode(url, "UTF-8");
			try {
				InputStream in = new URL(decodedURL).openStream();
				Files.copy(in, Paths.get(path));
				return "download complete";

			} catch (NoSuchFileException e) {
				LOG.error("cannot find the file " + path, e);
				return "download unsuccesfull";
			} catch (FileAlreadyExistsException e) {
				LOG.error("File already exists " + path, e);
				return "download unsuccesfull";
			} catch (IOException e) {
				LOG.error("Something went wrong when downloading file " + decodedURL + " to path " + path, e);
				return "download unsuccesfull";
			} catch (Exception e) {
				LOG.error(e.getMessage());
				return "download unsuccesfull";
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "download unsuccesfull";
		}

	}


	public CompletableFuture<String> fetchPromise(String endpoint) {
		return CompletableFuture.supplyAsync(() -> get(endpoint));
	}

	public CompletableFuture<String> fetchDownloadPromise(String url, String path) {
		return CompletableFuture.supplyAsync(() -> downloadImage(url, path));
	}

	public CompletableFuture<String> emptyResult() {
		return CompletableFuture.supplyAsync(() -> noResult());
	} 

	public String noResult(){
		return "";
	}

	public String get(String endpoint) {
		RestTemplate restTemplate = new RestTemplate();
		RequestEntity<String> request = new RequestEntity<>(HttpMethod.GET, URI.create(endpoint));
		ResponseEntity<String> jsonContent = restTemplate.exchange(request, String.class);
		return jsonContent.getBody();
	}

	public List<String> getJsonArray(Command command,  Map<String, String> variables) throws IOException {
		List<String> jsonNodes = regexUtils.splitByDelimiter(command.getParameter(), ".");
		List<String> nodesToParse = jsonNodes.stream().skip(1).collect(Collectors.toList());

		String dataToBeParsed = variables.get(jsonNodes.get(0));

		ObjectMapper objectMapper = JsonUtils.getObjectMapper();
		JsonNode rootNode = objectMapper.readTree(dataToBeParsed);

		for (String path : nodesToParse) {
			rootNode = rootNode.path(path);
		}

		List<String> jsonArray = new ArrayList<>();
		return loadArray(rootNode, jsonArray);
	}

	private  List<String> loadArray(JsonNode arrNode, List<String> jsonArray){
		if (arrNode.isArray()) {
			for (final JsonNode objNode : arrNode) {
				jsonArray.add(objNode.toString());
			}
		}
		return jsonArray;
	}




}