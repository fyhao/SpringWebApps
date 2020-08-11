package com.fyhao.springwebapps.dto;

import java.io.Serializable;

public class AgentProfileDto implements Serializable {
    String name;
    String status;
    String taskid;
    String targetagentid;
    String targetskill;

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

    
}