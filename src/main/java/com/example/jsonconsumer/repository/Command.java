package com.example.jsonconsumer.repository;

public class Command {

	private String command;
	private String variableName;
	private String parameter;
	private Command previousCommand;
	private Command nextCommand;

	public String getCommand() {
		return command;
	}

	public Command(String command, String variableName, String parameter) {
		super();
		this.command = command;
		this.variableName = variableName;
		this.parameter = parameter;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public Command getPreviousCommand() {
		return previousCommand;
	}

	public void setPreviousCommand(Command previousCommand) {
		this.previousCommand = previousCommand;
	}

	public Command getNextCommand() {
		return nextCommand;
	}

	public void setNextCommand(Command nextCommand) {
		this.nextCommand = nextCommand;
	}


}
