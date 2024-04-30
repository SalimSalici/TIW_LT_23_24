package it.polimi.tiw.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<User> fetchAllUsers() throws SQLException {
		List<User> users = new LinkedList<>();
		String query = "SELECT  id, username, email, name, surname FROM users ORDER BY surname ASC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					users.add(
						new User(
							result.getInt("id"),
							result.getString("username"),
							result.getString("email"),
							result.getString("name"),
							result.getString("surname")
						)
					);
				}
			}
		}
		return users;
	}

	public User login(String username, String password) throws SQLException {
		String query = "SELECT  id, username, email, name, surname FROM users WHERE username = ? AND password = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setEmail(result.getString("email"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}
	
	public boolean isUsernameAvailable(String username) throws SQLException {
		String query = "SELECT id FROM users WHERE username = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return true; // no results, username is available
				else {
					return false; // result found, username is not available
				}
			}
		}
	}

	public void register(String username, String email, String password, String name, String surname) throws SQLException {
		String query = "INSERT INTO users(username, email, password, name, surname) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);
			pstatement.setString(4, name);
			pstatement.setString(5, surname);
			pstatement.executeUpdate();
		}
	}
	
	public List<Group> fetchGroupsOwnedBy(int userId) throws SQLException {
		List<Group> groups = new LinkedList<>();
		String query = "SELECT id, group_name, `groups`.user_id, duration, min_users, max_users, created_at, COUNT(user_groups.group_id) AS user_count "
				+ "FROM `groups` LEFT JOIN user_groups ON user_groups.group_id = `groups`.id "
				+ "WHERE `groups`.user_id = ? "
				+ "AND DATE_ADD(`groups`.created_at, INTERVAL `groups`.duration DAY) >= CURDATE()"
				+ "GROUP BY id";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					int id = result.getInt("id");
					String group_name = result.getString("group_name");
					int user_id = result.getInt("user_id");
					int duration = result.getInt("duration");
					int min_users = result.getInt("min_users");
					int max_users = result.getInt("max_users");
					Date created_at = result.getDate("created_at");
					int user_count = result.getInt("user_count");
					Group gr = new Group(id, group_name, user_id, duration, min_users, max_users, created_at);
					gr.setUserCount(user_count);
					groups.add(gr);
				}
			}
		}
		return groups;
	}
	
	public List<Group> fetchGroupsWithUser(int userId) throws SQLException {
		List<Group> groups = new LinkedList<>();
		String query =
				   "SELECT g.id, g.group_name, g.duration, g.min_users, g.max_users, g.created_at, u.user_count, ug.user_id "
	             + "FROM `groups` g "
	             + "JOIN ("
	             		// This subquery is to extract the member count of each group
	             + "    SELECT group_id, COUNT(user_id) AS user_count " 
	             + "    FROM user_groups "
	             + "    GROUP BY group_id"
	             + ") u ON g.id = u.group_id "
	             + "JOIN user_groups ug ON g.id = ug.group_id "
	             + "WHERE ug.user_id = ? "
	             + "AND DATE_ADD(g.created_at, INTERVAL g .duration DAY) >= CURDATE()";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					int id = result.getInt("id");
					String group_name = result.getString("group_name");
					int user_id = result.getInt("user_id");
					int duration = result.getInt("duration");
					int min_users = result.getInt("min_users");
					int max_users = result.getInt("max_users");
					Date created_at = result.getDate("created_at");
					int user_count = result.getInt("user_count");
					Group gr = new Group(id, group_name, user_id, duration, min_users, max_users, created_at);
					gr.setUserCount(user_count);
					groups.add(gr);
				}
			}
		}
		return groups;
	}
	
	
}
