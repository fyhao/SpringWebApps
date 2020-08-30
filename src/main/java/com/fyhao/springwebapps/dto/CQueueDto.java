package com.fyhao.springwebapps.dto;

import java.io.Serializable;

public class CQueueDto implements Serializable {

	String name;
	long maxwaittime;
	String skilllist;
	long maxlimit;
	long priority;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getMaxwaittime() {
		return maxwaittime;
	}
	public void setMaxwaittime(long maxwaittime) {
		this.maxwaittime = maxwaittime;
	}
	public String getSkilllist() {
		return skilllist;
	}
	public void setSkilllist(String skilllist) {
		this.skilllist = skilllist;
	}
	public long getMaxlimit() {
		return maxlimit;
	}
	public void setMaxlimit(long maxlimit) {
		this.maxlimit = maxlimit;
	}
	public long getPriority() {
		return priority;
	}
	public void setPriority(long priority) {
		this.priority = priority;
	}
	
}
