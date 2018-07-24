package com.example.jsonconsumer.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;




@Service
@Configuration
public class CommandInterpreter {

	private static final Logger LOG = LoggerFactory.getLogger(CommandInterpreter.class);
	
	private static final String FETCH_COMMAND = "fetch";
	private static final String FOREACH_COMMAND = "foreach";
	private static final String DOWNLOAD_COMMAND = "download";

	@Value("${filepath}")
	public String fileName;

	@Autowired
	CommandExecutor commandExecutor;

	public void interpretCommands() throws Exception {

		final Map<String, String> variables = new HashMap<>();
		final List<String> commandsList = this.retrieveCommands(fileName);
		final List<Command> commands = new ArrayList<>();
		for (final String line : commandsList) {
			StringTokenizer stringTokenizer = new StringTokenizer(line);
			while (stringTokenizer.hasMoreElements()) {
				String command = stringTokenizer.nextElement().toString();
				String resultVariable = stringTokenizer.nextElement().toString();
				String parameter = stringTokenizer.nextElement().toString();
				Command cmd = new Command(command, resultVariable, parameter);
				commands.add(cmd);
			}
		}

		populateCommandRelationships(commands);
		processCommand(commands.get(0), variables);

	}

	private List<String> retrieveCommands(final String fileName) throws IOException {

		final File file = ResourceUtils.getFile(fileName);
		final InputStreamReader reader = new InputStreamReader(openInputStream(file),Charset.forName("UTF-8"));
		final BufferedReader bufferedReader = new BufferedReader(reader);

		final List<String> list = new ArrayList<>();
		String line = bufferedReader.readLine();
		while (line != null) {
			list.add(line);
			line = bufferedReader.readLine();
		}

		return list;
	}

	private static FileInputStream openInputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	private static void populateCommandRelationships(List<Command> commands) {
		for (int i = 0; i < commands.size(); i++) {
			Command command = commands.get(i);

			if (i > 0) {
				command.setPreviousCommand(commands.get(i - 1));
			}

			if (i < commands.size() - 1) {
				command.setNextCommand(commands.get(i + 1));
			}
		}
	}

	private void executeNextCommand(Command previouseCommand, Map<String, String> variables, String previouseCommandRespone){
		if (previouseCommandRespone != null && previouseCommandRespone != ""){
		variables.put(previouseCommand.getVariableName(), previouseCommandRespone);
		try {
			processCommand(previouseCommand.getNextCommand(), variables);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		}
	}

	private void processCommand(Command command, Map<String, String> variables) throws Exception {

		System.out.println("command.getCommand() = " + command.getCommand());
		System.out.println("command.getVariableName() = " + command.getVariableName());
		System.out.println("command.getParameter() = " + command.getParameter());

		if (command.getCommand().equals(FETCH_COMMAND)) {

			CompletableFuture<String> result = commandExecutor.fetch(command, variables);
			result.thenAccept(response -> executeNextCommand(command, variables, response));
		
		} else if (command.getCommand().equals(FOREACH_COMMAND)) {

			List<String> jsonArray = commandExecutor.getJsonArray(command, variables);
			for (String json : jsonArray) {
				executeNextCommand(command, variables, json);
			} 
		} else if (command.getCommand().equals(DOWNLOAD_COMMAND)) {
			CompletableFuture<String> result = commandExecutor.download(command, variables);
			result.thenAccept(response -> processCompleted());


		}
		
		
	}
	
	private void processCompleted(){
		System.out.println("Downloading complete");
	}


}
