package it.polimi.tiw.formbeans;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to validate the form for creating a new group.
 * It checks the name, duration, and number of users in the group.
 * If any of the parameters are invalid, an error message is added to the list of error messages.
 */
public class CreateGroupFormValidation {
	private String name;
	private int duration;
	private int minUsers;
	private int maxUsers;
	private List<String> errorMessages = new LinkedList<>();

	public CreateGroupFormValidation(String name, int duration, int minUsers, int maxUsers) {
		this.setName(name);;
		this.setDuration(duration);
		this.setMinAndMaxUsers(minUsers, maxUsers);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null)
			this.errorMessages.add("Missing group name.");
		else if (name.length() < 1 || name.length() > 100)
			this.errorMessages.add("Group name must be within 1 and 100 characters long.");
		
		this.name = name;
	}

	public int getMinUsers() {
		return minUsers;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setMinAndMaxUsers(int minUsers, int maxUsers) {
		if (minUsers < 1)
			this.errorMessages.add("Minimum amount of members must be at least 1.");
		if (maxUsers < 1)
			this.errorMessages.add("Maxmum amount of members must be at least 1.");
		if (maxUsers < minUsers)
			this.errorMessages.add("Maximum amount of members cannot be less than minimum amount of members.");
		
		this.minUsers = minUsers;
		this.maxUsers = maxUsers;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		if (duration < 1)
			this.errorMessages.add("Duration must be at least 1 day.");
		
		this.duration = duration;
	}
	
	public void addErrorMessage(String msg) {
		this.errorMessages.add(msg);
	}
	
	public List<String> getErrorMessages() {
		return errorMessages;
	}
	
	public boolean hasErrors() {
		return !this.errorMessages.isEmpty();
	}

}
