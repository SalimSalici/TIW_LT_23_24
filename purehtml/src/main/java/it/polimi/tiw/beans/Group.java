package it.polimi.tiw.beans;

import java.sql.Date;
import java.util.Calendar;

public class Group {
	private int id;
	private String groupName;
	private int userId;
	private int duration;
	private int minUsers;
	private int maxUsers;
	private Date created_at;
	private Date endDate;
	private int userCount;
	
	public Group(int id, String groupName, int userId, int duration, int minUsers, int maxUsers, Date created_at, int userCount) {
		this.id = id;
		this.groupName = groupName;
		this.setUserId(userId);
		this.duration = duration;
		this.minUsers = minUsers;
		this.maxUsers = maxUsers;
		this.created_at = created_at;
		this.userCount = userCount;

		// Calculate end date which is 'duration' days after created_at
        Calendar cal = Calendar.getInstance();
        cal.setTime(created_at);
        cal.add(Calendar.DAY_OF_MONTH, duration);
        this.endDate = new Date(cal.getTimeInMillis());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
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

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Group [id=" + id + ", groupName=" + groupName + ", userId=" + userId + ", duration=" + duration
				+ ", minUsers=" + minUsers + ", maxUsers=" + maxUsers + ", created_at=" + created_at + ", endDate="
				+ endDate + "]";
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
}
