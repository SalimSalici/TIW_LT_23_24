package it.polimi.tiw.beans;

public class RegisterFormValidation {
	private String username;
	private String name;
	private String surname;
	private String email;

	private String usernameError;
	private String nameError;
	private String surnameError;
	private String emailError;
	private String passwordError;
	private String confirmPasswordError;
	
	private boolean hasErrors = false;
	
	public RegisterFormValidation(String username, String name, String surname, String email) {
		this.username = username;
		this.name = name;
		this.surname = surname;
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsernameError() {
		return usernameError;
	}

	public void setUsernameError(String usernameError) {
		this.hasErrors = true;
		this.usernameError = usernameError;
	}

	public String getNameError() {
		return nameError;
	}

	public void setNameError(String nameError) {
		this.hasErrors = true;
		this.nameError = nameError;
	}

	public String getSurnameError() {
		return surnameError;
	}

	public void setSurnameError(String surnameError) {
		this.hasErrors = true;
		this.surnameError = surnameError;
	}

	public String getEmailError() {
		return emailError;
	}

	public void setEmailError(String emailError) {
		this.hasErrors = true;
		this.emailError = emailError;
	}

	public String getPasswordError() {
		return passwordError;
	}

	public void setPasswordError(String passwordError) {
		this.hasErrors = true;
		this.passwordError = passwordError;
	}

	public String getConfirmPasswordError() {
		return confirmPasswordError;
	}

	public void setConfirmPasswordError(String confirmPasswordError) {
		this.hasErrors = true;
		this.confirmPasswordError = confirmPasswordError;
	}

	public boolean hasErrors() {
		return this.hasErrors;
	}
}
