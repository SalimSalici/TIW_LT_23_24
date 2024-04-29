package it.polimi.tiw.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;

public class GroupDAO {
	private Connection connection;

	public GroupDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Group fetchGroupById(int groupId) throws SQLException {
		String query = "SELECT id, group_name, user_id, duration, min_users, max_users, created_at"
				+ " FROM `groups` WHERE id = ? ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, groupId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					int id = result.getInt("id");
					String group_name = result.getString("group_name");
					int user_id = result.getInt("user_id");
					int duration = result.getInt("duration");
					int min_users = result.getInt("min_users");
					int max_users = result.getInt("max_users");
					Date created_at = result.getDate("created_at");
					
					return new Group(
							id, 
							group_name, 
							user_id, 
							duration, 
							min_users, 
							max_users, 
							created_at
					);
				} else
					return null;
			}
		}
	}
	
	public List<User> fetchUsersOfGroup(int groupId) throws SQLException {
		List<User> users = new LinkedList<>();
		String query = "SELECT `users`.id, `users`.username, email, `users`.name, `users`.surname FROM users "
				+ "JOIN user_groups ON `users`.id = user_groups.user_id "
				+ "WHERE user_groups.group_id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, groupId);
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

	public List<Group> fetchGroupsOwnedBy(int userId) throws SQLException {
		List<Group> groups = new LinkedList<>();
		String query = "SELECT id, group_name, `groups`.user_id, duration, min_users, max_users, created_at, COUNT(user_groups.group_id) AS user_count "
				+ "FROM `groups` " + "LEFT JOIN user_groups ON user_groups.group_id = `groups`.id "
				+ "WHERE `groups`.user_id = ? " + "GROUP BY id";

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

	public boolean isGroupNameAvailableForUser(int user_id, String name) throws SQLException {
		String query = "SELECT * FROM `groups` WHERE user_id = ? AND group_name = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, user_id);
			pstatement.setString(2, name);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return true; // no results, username is available
				else
					return false; // result found, username is not available
			}
		}
	}

	public void createNewGroup(String name, int creator_id, int duration, int minUsers, int maxUsers, int[] userIds)
			throws SQLException {

		this.connection.setAutoCommit(false);

		String queryGroup = "INSERT INTO `groups`(group_name, user_id, duration, min_users, max_users) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstatementGroup = connection.prepareStatement(queryGroup, Statement.RETURN_GENERATED_KEYS);
		String queryUsers = "INSERT INTO `user_groups`(user_id, group_id) VALUES (?, ?)";
		PreparedStatement pstatementUsers = connection.prepareStatement(queryUsers);
		try {

			// Insert new group
			pstatementGroup.setString(1, name);
			pstatementGroup.setInt(2, creator_id);
			pstatementGroup.setInt(3, duration);
			pstatementGroup.setInt(4, minUsers);
			pstatementGroup.setInt(5, maxUsers);
			pstatementGroup.executeUpdate();
			
			// Retrieve id of newly inserted group
			ResultSet keys = pstatementGroup.getGeneratedKeys();
			
			if (!keys.isBeforeFirst())
				throw new SQLException("Couldn't retrieve id of newly created group.");
			
			keys.next();
			int generatedGroupId = keys.getInt(1); // id is in the first column of the ResultSet
			
			for (int userId : userIds) {
				pstatementUsers.setInt(1, userId);
				pstatementUsers.setInt(2, generatedGroupId);
				pstatementUsers.executeUpdate();
			}
			
			this.connection.commit();

		} catch (SQLException e) {
			this.connection.rollback();
			throw e;
		} finally {
			pstatementGroup.close();
			pstatementUsers.close();
			this.connection.setAutoCommit(true);
		}

	}
}
