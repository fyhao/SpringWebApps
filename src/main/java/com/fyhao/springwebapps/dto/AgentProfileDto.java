package com.fyhao.springwebapps.dto;

import java.io.Serializable;

public class AgentProfileDto implements Serializable {
    String name;
    String status;

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
    
}