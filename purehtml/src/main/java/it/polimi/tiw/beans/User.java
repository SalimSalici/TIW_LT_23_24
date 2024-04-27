package it.polimi.tiw.beans;

public class User {

	private int id;
	private String username;
	private String email;
	private String name;
	private String surname;

	public User() {}
	
	public User(int id, String username, String email, String name, String surname) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.name = name;
		this.surname = surname;
	}

	public int getId() {
		return this.id;
	}

	public String getUsername() {
		return this.username;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
	
	@Override
	public String toString() {
		return "User[" 
				+ this.id + ", " 
				+ this.username + ", " 
				+ this.email + ", " 
				+ this.name + ", " 
				+ this.surname + "]";
	}
}
