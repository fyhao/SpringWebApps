package com.fyhao.springwebapps.dto;

import java.io.Serializable;

public class AgentProfileDto implements Serializable {
    String name;
    String status;
    String taskid;
    String targetagentid;
    String targetskill;
    String conversationid;
    int maxconcurrenttask;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public String getTaskid() {
		return taskid;
	}

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	public String getTargetagentid() {
		return targetagentid;
	}

	public void setTargetagentid(String targetagentid) {
		this.targetagentid = targetagentid;
	}

    public String getTargetskill() {
        return targetskill;
    }

    public void setTargetskill(String targetskill) {
        this.targetskill = targetskill;
    }

    public String getConversationid() {
        return conversationid;
    }

    public void setConversationid(String conversationid) {
        this.conversationid = conversationid;
    }

	public int getMaxconcurrenttask() {
		return maxconcurrenttask;
	}

	public void setMaxconcurrenttask(int maxconcurrenttask) {
		this.maxconcurrenttask = maxconcurrenttask;
	}

    
}