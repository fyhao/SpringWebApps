package com.fyhao.springwebapps.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.CCConfigDto;
import com.fyhao.springwebapps.dto.SkillDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.Skill;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.model.SkillRepository;

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
        Agent agent = agentRepository.findByName(agentDto.getName());
        if (agent != null) {
            return;
        }
        agent = modelMapper().map(agentDto, Agent.class);
        agent.setName(agent.getName().isEmpty() ? "Unnamed" : agent.getName());
        agentRepository.save(agent);
    }

    public void createSkillProfile(SkillDto skillDto) {
        Skill skill = skillRepository.findByName(skillDto.getName());
        if (skill != null) {
            return;
        }
        skill = modelMapper().map(skillDto, Skill.class);
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
        if(agentSkillDto.getAction().equals(AgentSkillDto.ASSIGNED_TO_SKILL)) {
            agentSkillDto.setAction(AgentSkillDto.ASSIGNED_TO_AGENT);
        }
        else if(agentSkillDto.getAction().equals(AgentSkillDto.REMOVED_FROM_SKILL)) {
            agentSkillDto.setAction(AgentSkillDto.REMOVED_FROM_AGENT);
        }
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
            agentRepository.save(agent);
        } else if (action.equals(AgentSkillDto.REMOVED_FROM_AGENT)) {
            if (agent.getAgentSkills().contains(skill)) {
                agent.getAgentSkills().remove(skill);
            }
            agentRepository.save(agent);
        }
        return 0;
    }
    public long getAgentCount() {
        return agentRepository.count();
    }
    public long getSkillCount() {
        return skillRepository.count();
    }
    public List<String> getSkillNamesOfAgent(String agentName) {
        Agent agent = agentRepository.findByName(agentName);
        if (agent == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        if(!agent.getAgentSkills().isEmpty()) {
            for(Skill skill : agent.getAgentSkills()) {
                list.add(skill.getName());
            }
        }
        return list;
    }
    public int getMaxConcurrentTaskOfAgent(String agentName) {
    	Agent agent = agentRepository.findByName(agentName);
        if (agent == null) {
            return -1;
        }
        return agent.getMaxConcurrentTask();
    }
    public int setMaxConcurrentTaskOfAgent(String agentName, int m) {
    	Agent agent = agentRepository.findByName(agentName);
        if (agent == null) {
            return -1;
        }
        agent.setMaxConcurrentTask(m);
        agentRepository.save(agent);
        return 0;
    }
    public Iterable<Agent> getAllAgents() {
        return agentRepository.findAll();
    }
    public Iterable<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    public CCConfigDto exportConfig() {
    	CCConfigDto res = new CCConfigDto();
    	for(Skill skill : skillRepository.findAll()) {
    		SkillDto d = new SkillDto();
    		d.setName(skill.getName());
    		res.getSkills().add(d);
    	}
    	for(Agent agent : agentRepository.findAll()) {
    		AgentProfileDto d = new AgentProfileDto();
    		d.setName(agent.getName());
    		d.setMaxconcurrenttask(agent.getMaxConcurrentTask());
    		res.getAgents().add(d);
    		Set<Skill> agentSkills = agent.getAgentSkills();
    		for(Skill skill : agentSkills) {
    			AgentSkillDto asDto = new AgentSkillDto();
    			asDto.setAgent(agent.getName());
    			asDto.setSkill(skill.getName());
    			asDto.setAction(AgentSkillDto.ASSIGNED_TO_AGENT);
    			res.getAgentSkills().add(asDto);
    		}
    	}
    	return res;
    }
    public int importConfig(CCConfigDto dto) {
    	// remove all skills from all agent first
    	for(Agent agent : agentRepository.findAll()) {
    		for(Skill skill : agent.getAgentSkills()) {
    			agent.getAgentSkills().remove(skill);
    		}
    		agentRepository.save(agent);
    		agentRepository.delete(agent);
    	}
    	for(Skill skill : skillRepository.findAll()) {
    		skillRepository.delete(skill);
    	}
    	// add new skill
    	for(SkillDto skillDto : dto.getSkills()) {
    		Skill skill = new Skill();
    		skill.setName(skillDto.getName());
    		skillRepository.save(skill);
    	}
    	// add new agent
    	for(AgentProfileDto agentDto : dto.getAgents()) {
    		Agent agent = new Agent();
    		agent.setName(agentDto.getName());
    		agent.setMaxConcurrentTask(agentDto.getMaxconcurrenttask());
    		agentRepository.save(agent);
    	}
    	for(AgentSkillDto asDto : dto.getAgentSkills()) {
    		assignAgentSkillAction(asDto);
    	}
    	return 0;
    }
}