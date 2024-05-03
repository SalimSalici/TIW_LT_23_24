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
		String query = 
				  "SELECT `groups`.id AS group_id, group_name, `groups`.user_id, duration, min_users, max_users, `groups`.created_at, COUNT(user_groups.user_id) AS user_count, "
				+ "username, name, surname, email FROM `groups` "
				+ "JOIN users ON users.id = `groups`.user_id "
				+ "LEFT JOIN user_groups ON user_groups.group_id = `groups`.id "
				+ "WHERE `groups`.id = ? "
				+ "GROUP BY `groups`.id";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, groupId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					int id = result.getInt("group_id");
					String group_name = result.getString("group_name");
					int userId = result.getInt("user_id");
					int duration = result.getInt("duration");
					int minUsers = result.getInt("min_users");
					int maxUsers = result.getInt("max_users");
					int userCount = result.getInt("user_count");
					Date createdAt = result.getDate("created_at");
					String ownerUsername = result.getString("username");
					String ownerName = result.getString("name");
					String ownerSurname = result.getString("surname");
					String ownerEmail = result.getString("email");
					
					Group group = new Group(
							id, 
							group_name, 
							userId, 
							duration, 
							minUsers, 
							maxUsers, 
							createdAt
					);		
					group.setOwner(new User(userId, ownerUsername, ownerEmail, ownerName, ownerSurname));
					group.setUserCount(userCount);
					return group;
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
		String queryUsers = "INSERT INTO `user_groups`(user_id, group_id) VALUES (?, ?)";
		
		try (
			PreparedStatement pstatementGroup = connection.prepareStatement(queryGroup, Statement.RETURN_GENERATED_KEYS);
			PreparedStatement pstatementUsers = connection.prepareStatement(queryUsers);
		){

			// Insert new group
			pstatementGroup.setString(1, name);
			pstatementGroup.setInt(2, creator_id);
			pstatementGroup.setInt(3, duration);
			pstatementGroup.setInt(4, minUsers);
			pstatementGroup.setInt(5, maxUsers);
			pstatementGroup.executeUpdate();
			
			// Retrieve id of newly inserted group
			try (ResultSet keys = pstatementGroup.getGeneratedKeys();) {
				
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
			}

		} catch (SQLException e) {
			this.connection.rollback();
			throw e;
		} finally {
			this.connection.setAutoCommit(true);
		}
	}
	
	public boolean removeMemberFromGroup(int groupId, int memberId) throws SQLException {
		String query = "DELETE FROM user_groups WHERE group_id = ? AND user_id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, groupId);
			pstatement.setInt(2, memberId);
			
			int deleted = pstatement.executeUpdate();
			if (deleted == 1) return true;
			else return false;
		}
	}
}
