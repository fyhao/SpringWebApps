package com.fyhao.springwebapps.dto;

import java.util.Date;

import com.fyhao.springwebapps.entity.Conversation;

public class QueueDto {

	Conversation conversation;
	String skillName;
	Date enteredTime;
	String status;
	public Date getEnteredTime() {
		return enteredTime;
	}
	public void setEnteredTime(Date enteredTime) {
		this.enteredTime = enteredTime;
	}
	public Conversation getConversation() {
		return conversation;
	}
	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}
	public String getSkillName() {
		return skillName;
	}
	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
