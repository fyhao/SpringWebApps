package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Skill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class AgentAvailabilityService {
    static Logger logger = LoggerFactory.getLogger(AgentAvailabilityService.class);
    @Autowired
    AgentTerminalService agentTerminalService;

    public String findAgent(Conversation conversation, String skillName) {
        logger.info("AgentAvailabilityService findAgent " + conversation.getId().toString() + " " + skillName);
        AgentTerminal term = agentTerminalService.getMostAvailableAgent(skillName);
        logger.info("AgentAvailabilityService findAgent term " + (term != null));
        if(term == null || term.getAgent() == null) return null;
        return term.getAgent().getName();
    }
}