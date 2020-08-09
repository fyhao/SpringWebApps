package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.entity.Conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentAvailabilityService {
    
    @Autowired
    AgentTerminalService agentTerminalService;

    public String findAgent(Conversation conversation) {
        AgentTerminal term = agentTerminalService.getMostAvailableAgent();
        if(term == null || term.getAgent() == null) return null;
        return term.getAgent().getName();
    }
}