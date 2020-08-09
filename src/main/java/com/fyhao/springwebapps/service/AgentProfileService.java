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

    public int removeAgentProfile(AgentProfileDto agentDto) {
        Agent agent = agentRepository.findByName(agentDto.getName());
        if (agent == null) {
            return 101;
        }
        agentRepository.delete(agent);
        return 0;
    }

    public int removeSkillProfile(SkillDto skillDto) {
        Skill skill = skillRepository.findByName(skillDto.getName());
        if (skill == null) {
            return 101;
        }
        skillRepository.delete(skill);
        return 0;
    }

    public int assignAgentSkillAction(AgentSkillDto agentSkillDto) {
        Agent agent = agentRepository.findByName(agentSkillDto.getAgent());
        if (agent == null) {
            return 101;
        }
        Skill skill = skillRepository.findByName(agentSkillDto.getSkill());
        if (skill == null) {
            return 102;
        }
        String action = agentSkillDto.getAction();
        if (action.equals(AgentSkillDto.ASSIGNED_TO_AGENT)) {
            if (!agent.getAgentSkills().contains(skill)) {
                agent.getAgentSkills().add(skill);
            }
        } else if (action.equals(AgentSkillDto.REMOVED_FROM_AGENT)) {
            if (agent.getAgentSkills().contains(skill)) {
                agent.getAgentSkills().remove(skill);
            }
        } else if (action.equals(AgentSkillDto.ASSIGNED_TO_SKILL)) {
            if (!skill.getAgents().contains(agent)) {
                skill.getAgents().add(agent);
            }
        } else if (action.equals(AgentSkillDto.REMOVED_FROM_SKILL)) {
            if (skill.getAgents().contains(agent)) {
                skill.getAgents().remove(agent);
            }
        }
        agentRepository.save(agent);
        skillRepository.save(skill);
        return 0;
    }
    public long getAgentCount() {
        return agentRepository.count();
    }
}