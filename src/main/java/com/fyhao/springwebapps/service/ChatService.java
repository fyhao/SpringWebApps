package com.fyhao.springwebapps.service;

import java.util.Date;

import com.fyhao.springwebapps.entity.Conversation;
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
        hookProcessor.executeHookCC("preChatProcessCustomerMessage", conversation, input);
        boolean hasFoundAgent = false;
        boolean isTransferAgent = false;
        if(input.equals("transferagent")) {
            isTransferAgent = true;
            conversation.saveContext("hint", "1");
            String agentName = agentAvailabilityService.findAgent(conversation);
            if(agentName != null) {
                conversation.saveContext("state", "agent");
                hasFoundAgent = true;
            }
        }
        else if(input.equals("transferagentfail")) {
            isTransferAgent = true;
            conversation.saveContext("hint", "0");
            String agentName = agentAvailabilityService.findAgent(conversation);
            if(agentName != null) {
                conversation.saveContext("state", "agent");
            }
        }
        /*else if(input.equals("bye")) {
            conversation.saveContext("state", "end");
            conversation.setEndTime(Util.getSQLTimestamp(new Date()));
        }*/
        conversation.addMessageWithInput(input);
        if(isTransferAgent) {
            if(hasFoundAgent) {
                conversation.addSystemMessageWithInput("you are chatting with our agent");
            }
            else {
                conversation.addSystemMessageWithInput("agent not available");
            }
        }
        hookProcessor.executeHookCC("postChatProcessCustomerMessage", conversation, input);
    }
    public void processSystemMessage(Conversation conversation, String input) {
        conversation.addSystemMessageWithInput(input);
    }
}