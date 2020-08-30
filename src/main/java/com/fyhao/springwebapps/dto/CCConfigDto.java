package com.fyhao.springwebapps.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CCConfigDto implements Serializable{

	List<AgentProfileDto> agents = new ArrayList<AgentProfileDto>();
	List<SkillDto> skills = new ArrayList<SkillDto>();
	List<AgentSkillDto> agentSkills = new ArrayList<AgentSkillDto>();
	List<CQueueDto> cqueues = new ArrayList<CQueueDto>();
	public List<AgentProfileDto> getAgents() {
		return agents;
	}
	public void setAgents(List<AgentProfileDto> agents) {
		this.agents = agents;
	}
	public List<SkillDto> getSkills() {
		return skills;
	}
	public void setSkills(List<SkillDto> skills) {
		this.skills = skills;
	}
	public List<AgentSkillDto> getAgentSkills() {
		return agentSkills;
	}
	public void setAgentSkills(List<AgentSkillDto> agentSkills) {
		this.agentSkills = agentSkills;
	}
	public List<CQueueDto> getCqueues() {
		return cqueues;
	}
	public void setCqueues(List<CQueueDto> cqueues) {
		this.cqueues = cqueues;
	}
	
}
