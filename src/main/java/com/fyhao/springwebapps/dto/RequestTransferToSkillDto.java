package com.fyhao.springwebapps.dto;

public class RequestTransferToSkillDto {
    private String name;
    private String taskid;
    private String targetskill;

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

    public String getTargetskill() {
        return targetskill;
    }

    public void setTargetskill(String targetskill) {
        this.targetskill = targetskill;
    }
}
