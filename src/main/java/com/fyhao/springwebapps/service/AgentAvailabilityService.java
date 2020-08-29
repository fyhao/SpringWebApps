package com.fyhao.springwebapps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.model.ConversationRepository;
@Service
public class AgentAvailabilityService {
    static Logger logger = LoggerFactory.getLogger(AgentAvailabilityService.class);
    @Autowired
    AgentTerminalService agentTerminalService;
    @Autowired
    ConversationRepository conversationRepository;

    public String findAgent(Conversation conversation, String skillName) {
        logger.info("AgentAvailabilityService findAgent " + conversation.getId().toString() + " " + skillName);
        AgentTerminal term = agentTerminalService.getMostAvailableAgent(skillName);
        logger.info("AgentAvailabilityService findAgent term " + (term != null));
        if(term == null || term.getAgent() == null) return null;
        return term.getAgent().getName();
    }
    public String queueSkill(Conversation conversation, String skillName) {
    	conversation.addActivity("conversationQueued");
    	conversationRepository.save(conversation);
    	String foundAgent = null;
    	foundAgent = findAgent(conversation, skillName);
    	if(foundAgent != null) {
    		conversation.addActivityWithAgent("conversationOffered", foundAgent);
    		return foundAgent;
    	}
    	long maxTimeToWait = 5000;
    	long now = System.currentTimeMillis();
    	
    	while(System.currentTimeMillis() - now < maxTimeToWait) {
    		try {Thread.sleep(100); } catch (Exception ex) {}
    		foundAgent = findAgent(conversation, skillName);
    		if(foundAgent != null) {
    			conversation.addActivityWithAgent("conversationOffered", foundAgent);
    			return foundAgent;
    		}
    	}
    	return foundAgent;
    }
}