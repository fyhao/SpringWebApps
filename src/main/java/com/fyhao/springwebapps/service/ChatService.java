package com.fyhao.springwebapps.service;

import java.util.Date;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.HookCC;
import com.fyhao.springwebapps.hook.HookProcessor;
import com.fyhao.springwebapps.util.Util;

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
        conversation.addSystemMessageWithInput(input);
    }
}