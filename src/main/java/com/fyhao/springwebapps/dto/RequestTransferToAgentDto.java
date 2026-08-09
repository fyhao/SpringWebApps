package com.fyhao.springwebapps.dto;

public class RequestTransferToAgentDto {
    private String name;
    private String taskid;
    private String targetagentid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
