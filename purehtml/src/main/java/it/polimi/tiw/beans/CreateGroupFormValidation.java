package it.polimi.tiw.beans;

import java.util.LinkedList;
import java.util.List;

public class CreateGroupFormValidation {
	private String name;
	private int duration;
	private int minUsers;
	private int maxUsers;
	private List<String> errorMessages;

	private boolean hasErrors = false;

	public CreateGroupFormValidation(String name, int duration, int minUsers, int maxUsers) {
		this.name = name;
		this.duration = duration;
		this.minUsers = minUsers;
		this.maxUsers = maxUsers;
		this.errorMessages = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinUsers() {
		return minUsers;
	}

	public void setMinUsers(int minUsers) {
		this.minUsers = minUsers;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void addErrorMessage(String errorMessage) {
		this.hasErrors = true;
		this.errorMessages.add(errorMessage);
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
