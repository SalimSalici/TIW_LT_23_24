package it.polimi.tiw.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import it.polimi.tiw.beans.Group;

public class GroupDAO {
	private Connection connection;

	public GroupDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Group> fetchGroupsOwnedBy(int userId) throws SQLException {
		List<Group> groups = new LinkedList<>();
		// String query = "SELECT id, group_name, user_id, duration, min_users, max_users, created_at FROM `groups` WHERE user_id = ?";
		String query = "SELECT id, group_name, `groups`.user_id, duration, min_users, max_users, created_at, COUNT(user_groups.group_id) AS user_count " +
							"FROM `groups` " +
							"LEFT JOIN user_groups ON user_groups.group_id = `groups`.id " +
							"WHERE `groups`.user_id = ? " +
							"GROUP BY id";

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
					Group gr = new Group(id, group_name, user_id, duration, min_users, max_users, created_at, user_count);
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
	
	public void createNewGroup(String name, int creator_id, int duration, int minUsers, int maxUsers) throws SQLException {
		String query = "INSERT INTO `groups`(group_name, user_id, duration, min_users, max_users) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, name);
			pstatement.setInt(2, creator_id);
			pstatement.setInt(3, duration);
			pstatement.setInt(4, minUsers);
			pstatement.setInt(5, maxUsers);
			pstatement.executeUpdate();
		}
	}
}
