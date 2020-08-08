package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.Conversation;

import org.springframework.stereotype.Service;

@Service
public class AgentAvailabilityService {
    
    public String findAgent(Conversation conversation) {
        String hint = conversation.findContext("hint");
        if(hint.equals("1")) {
            return "sjeffers";
        }
        else if(hint.equals("0")) {
            return null;
        }
        return null;
    }
}