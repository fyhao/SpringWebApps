package com.fyhao.springwebapps.dto;

import java.io.Serializable;

public class CQueueDto implements Serializable {

	String name;
	long maxwaittime;
	String skilllist;
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
	
}
