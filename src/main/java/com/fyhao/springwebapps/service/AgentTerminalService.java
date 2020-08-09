package com.fyhao.springwebapps.service;

import java.util.ArrayList;
import java.util.List;

import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.model.AgentTerminalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentTerminalService {
    static Logger logger = LoggerFactory.getLogger(AgentTerminalService.class);
    @Autowired
    AgentRepository agentRepository;
    @Autowired
    AgentTerminalRepository agentTerminalRepository;

    public int registerAgent(String agentName) {
        logger.info("AgentTerminalService registerAgent " + agentName);
        Agent agent = agentRepository.findByName(agentName);
        if(agent == null) {
            return 101;
        }
        if(agent.getAgentTerminal() == null) {
            AgentTerminal agentTerminal = new AgentTerminal();
            agentTerminal.setStatus(AgentTerminal.NOT_READY);
            agent.setAgentTerminal(agentTerminal);
            agentTerminal.setAgent(agent);
            agentTerminalRepository.save(agentTerminal);
        }
        return 0;
    }

    public int unregisterAgent(String agentName) {
        logger.info("AgentTerminalService unregisterAgent " + agentName);
        Agent agent = agentRepository.findByName(agentName);
        if(agent == null) {
            return 101;
        }
        if(agent.getAgentTerminal() != null) {
            AgentTerminal terminal = agent.getAgentTerminal();
            // #54 agent unregister rule, must be not ready then can unregister
            if(!terminal.getStatus().equals(AgentTerminal.NOT_READY)) {
                return 102;
            }
            agent.setAgentTerminal(null);
            agentRepository.save(agent);
            agentTerminalRepository.delete(terminal);
        }
        return 0;
    }

    public int setAgentStatus(String agentName, String status) {
        Agent agent = agentRepository.findByName(agentName);
        if(agent == null) {
            return 101;
        }
        if(agent.getAgentTerminal() != null) {
            agent.getAgentTerminal().setStatus(status);
            agentTerminalRepository.save(agent.getAgentTerminal());
        }
        return 0;
    }

    public String getAgentStatus(String agentName) {
        Agent agent = agentRepository.findByName(agentName);
        if(agent == null) {
            return null;
        }
        if(agent.getAgentTerminal() != null) {
            return agent.getAgentTerminal().getStatus();
        }
        return null;
    }
    public long getAgentTerminalsCount() {
        return agentTerminalRepository.count();
    }
    public List<String> getActiveAgentTerminalNames() {
        List<String> names = new ArrayList<String>();
        for(AgentTerminal term : agentTerminalRepository.findAll()) {
            Agent agent = term.getAgent();
            names.add(agent.getName());
        }
        return names;
    }
}