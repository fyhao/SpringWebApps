package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.HookCC;
import com.fyhao.springwebapps.hook.HookCS;
import com.fyhao.springwebapps.hook.HookProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    
    @Autowired
    AgentAvailabilityService agentAvailabilityService;

    @Autowired
    HookProcessor hookProcessor;
    
    public void processCustomerMessage(Conversation conversation, String input) {
        hookProcessor.execute(HookCC.class, "preChatProcessCustomerMessage", conversation, input);
        conversation.addMessageWithInput(input);
        hookProcessor.execute(HookCC.class, "postChatProcessCustomerMessage", conversation, input);
    }
    public void processSystemMessage(Conversation conversation, String input) {
        hookProcessor.execute(HookCS.class, "preChatProcessSystemMessage", conversation, input);
        conversation.addSystemMessageWithInput(input);
        hookProcessor.execute(HookCS.class, "postChatProcessSystemMessage", conversation, input);
    }
    public void processAgentMessage(Conversation conversation, String agentName, String input) {
        conversation.addAgentMessageWithInput(agentName, input);
    }
    public void processBotMessage(Conversation conversation, String input) {
        conversation.addBotMessageWithInput(input);
    }
}