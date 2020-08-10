package com.fyhao.springwebapps.dto;

import java.io.Serializable;
public class AgentSkillDto implements Serializable {
    String agent;
    String skill;
    String action; // ASSIGNED_TO_AGENT / REMOVED_FROM_AGENT / ASSIGNED_TO_SKILL / REMOVED_FROM_SKILL

    public final static String ASSIGNED_TO_AGENT = "ASSIGNED_TO_AGENT";
    public final static String REMOVED_FROM_AGENT = "REMOVED_FROM_AGENT";
    public final static String ASSIGNED_TO_SKILL = "ASSIGNED_TO_SKILL";
    public final static String REMOVED_FROM_SKILL = "REMOVED_FROM_SKILL";

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
}