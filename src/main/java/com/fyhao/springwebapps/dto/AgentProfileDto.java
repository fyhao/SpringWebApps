package com.fyhao.springwebapps.dto;

import java.io.Serializable;

public class AgentProfileDto implements Serializable {
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}