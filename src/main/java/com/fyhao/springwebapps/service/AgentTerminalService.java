package com.fyhao.springwebapps.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.model.AgentTerminalRepository;
import com.fyhao.springwebapps.ws.AgentSocketHandler;

@Service
public class AgentTerminalService {
    static Logger logger = LoggerFactory.getLogger(AgentTerminalService.class);
    @Autowired
    AgentRepository agentRepository;
    @Autowired
    AgentTerminalRepository agentTerminalRepository;
    @Autowired
    EventPublisher publisher;
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
            publisher.publishEvent("agentRegistered");
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
                AgentSocketHandler.sendAgentUnregisteredEvent(agentName, false, "Failed as agent is not in not ready state");
                return 102;
            }
            agent.setAgentTerminal(null);
            agentTerminalRepository.delete(terminal);
            agentRepository.save(agent);
            AgentSocketHandler.sendAgentUnregisteredEvent(agentName, true, "SUCCESS");
        }
        return 0;
    }

    public int setAgentStatus(String agentName, String status) {
        logger.info("AgentTerminalService setAgentStatus " + agentName + " " + status);
        Agent agent = agentRepository.findByName(agentName);
        if(agent == null) {
            return 101;
        }
        if(agent.getAgentTerminal() != null) {
            String oldstatus = agent.getAgentTerminal().getStatus();
            agent.getAgentTerminal().setStatus(status);
            agentTerminalRepository.save(agent.getAgentTerminal());
            AgentSocketHandler.sendAgentStatusChangedEvent(agentName, oldstatus, status);
            if(status.equals("READY")) {
            	publisher.publishEvent("agentReady");
            }
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
    public AgentTerminal getMostAvailableAgent(String skillName) {
        logger.info("AgentTerminalService.getMostAvailableAgent " + skillName);
        List<AgentTerminal> terms = agentTerminalRepository.findByStatus(AgentTerminal.READY);
        if(terms == null || terms.isEmpty()) return null;
        for(AgentTerminal term : terms) {
            logger.info("AgentTerminalService Checking agent term " + term.getAgent().getName() + " if has skill " + skillName);
            if(!term.getAgent().hasSkill(skillName)) {
                continue;
            }
            int activeTaskCount = term.getAgent().getActiveTaskCount();
            int maxConcurrentTask = term.getAgent().getMaxConcurrentTask();
            if(activeTaskCount >= maxConcurrentTask) {
            	continue;
            }
            logger.info("AgentTerminalService found " + term.getAgent().getName());
            return term;
        }
        logger.info("AgentTerminalService not found");
        return null;
    }
}