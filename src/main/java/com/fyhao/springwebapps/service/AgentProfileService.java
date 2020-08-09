package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.SkillDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.Skill;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.model.SkillRepository;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class AgentProfileService {
    
    @Autowired
    AgentRepository agentRepository;

    @Autowired
    SkillRepository skillRepository;
    
    @Autowired
    private ModelMapper modelMapper;

	@Bean
	public ModelMapper modelMapper() {
	  ModelMapper m = new ModelMapper();
	  m.getConfiguration().setPropertyCondition(Conditions.isNotNull());
	  return m;
	}

    public void createAgentProfile(AgentProfileDto agentDto) {
        Agent agent = modelMapper().map(agentDto, Agent.class);
        agent.setName(agent.getName().isEmpty() ? "Unnamed" : agent.getName());
        agentRepository.save(agent);
    }

    public void createSkillProfile(SkillDto skillDto) {
        Skill skill = modelMapper().map(skillDto, Skill.class);
        skill.setName(skill.getName().isEmpty() ? "Unnamed" : skill.getName());
        skillRepository.save(skill);
    }

    public void assignAgentSkillAction(AgentSkillDto agentSkillDto) {
        
    }
}