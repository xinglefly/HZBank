package com.byteera.bank.domain;

import java.io.Serializable;
import java.util.List;

public class MeetingContent implements Serializable {
	private static final long serialVersionUID = 4955837554523890274L;

	private String meet_name;
	private String meet_datetime;
	private String meet_desc;
	private String meet_id;
	private String chat_group_name;
	private Integer members;
	private String meet_circle_tag;
	private String filePath;
	private int expired;	//1表示过期，0未过期

	private List<String> list;


	public int getExpired() {
		return expired;
	}
	public void setExpired(int expired) {
		this.expired = expired;
	}
	public String getMeet_circle_tag() {
		return meet_circle_tag;
	}
	public void setMeet_circle_tag(String meet_circle_tag) {
		this.meet_circle_tag = meet_circle_tag;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
	public String getMeet_name() {
		return meet_name;
	}
	public void setMeet_name(String meet_name) {
		this.meet_name = meet_name;
	}
	public String getMeet_datetime() {
		return meet_datetime;
	}
	public void setMeet_datetime(String meet_datetime) {
		this.meet_datetime = meet_datetime;
	}
	public String getMeet_desc() {
		return meet_desc;
	}
	public void setMeet_desc(String meet_desc) {
		this.meet_desc = meet_desc;
	}
	public String getMeet_id() {
		return meet_id;
	}
	public void setMeet_id(String meet_id) {
		this.meet_id = meet_id;
	}
	public String getChat_group_name() {
		return chat_group_name;
	}
	public void setChat_group_name(String chat_group_name) {
		this.chat_group_name = chat_group_name;
	}
	public Integer getMembers() {
		return members;
	}
	public void setMembers(Integer members) {
		this.members = members;
	}
	@Override
	public String toString() {
		return "MeetingContent [meet_name=" + meet_name + ", meet_datetime="
				+ meet_datetime + ", meet_desc=" + meet_desc + ", meet_id="
				+ meet_id + ", chat_group_name=" + chat_group_name
				+",expired="+expired
				+ ", members=" + members + "]";
	}

	
	
	public MeetingContent(String meet_name, String meet_datetime,
                          String meet_desc, String meet_id, String chat_group_name,
                          Integer members, List<String> list, String meet_circle_tag,
                          String filePath, int expired) {
		super();
		this.meet_name = meet_name;
		this.meet_datetime = meet_datetime;
		this.meet_desc = meet_desc;
		this.meet_id = meet_id;
		this.chat_group_name = chat_group_name;
		this.members = members;
		this.list = list;
		this.meet_circle_tag = meet_circle_tag;
		this.expired = expired;
        this.filePath = filePath;
	}


	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
