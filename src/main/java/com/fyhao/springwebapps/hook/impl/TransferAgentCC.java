package com.fyhao.springwebapps.hook.impl;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.ChatCustomerHook;
import com.fyhao.springwebapps.hook.HookCC;
import com.fyhao.springwebapps.service.AgentAvailabilityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@HookCC
public class TransferAgentCC implements ChatCustomerHook {
    static Logger logger = LoggerFactory.getLogger(TransferAgentCC.class);
    @Autowired
    AgentAvailabilityService agentAvailabilityService;
    @Override
    public void preChatProcessCustomerMessage(Conversation conversation, String input) {
        boolean hasFoundAgent = conversation.findContextBool("hasFoundAgent");
        boolean isTransferAgent = conversation.findContextBool("isTransferAgent");
        if(input.equals("transferagent")) {
            isTransferAgent = true;
            conversation.saveContext("hint", "1");
            String agentName = agentAvailabilityService.findAgent(conversation);
            if(agentName != null) {
                conversation.saveContext("state", "agent");
                conversation.saveContext("agentName", agentName);
                hasFoundAgent = true;
            }
        }
        else if(input.equals("transferagentfail")) {
            isTransferAgent = true;
            conversation.saveContext("hint", "0");
            String agentName = agentAvailabilityService.findAgent(conversation);
            if(agentName != null) {
                conversation.saveContext("state", "agent");
                conversation.saveContext("agentName", agentName);
            }
        }
        conversation.saveContextBool("hasFoundAgent", hasFoundAgent);
        conversation.saveContextBool("isTransferAgent", isTransferAgent);
    }

    @Override
    public void postChatProcessCustomerMessage(Conversation conversation, String input) {
        boolean hasFoundAgent = conversation.findContextBool("hasFoundAgent");
        boolean isTransferAgent = conversation.findContextBool("isTransferAgent");
        boolean isTransferAgentSent = conversation.findContextBool("isTransferAgentSent");
        if(isTransferAgent) {
            if(hasFoundAgent) {
                if(!isTransferAgentSent) {
                    conversation.addSystemMessageWithInput("you are chatting with our agent");
                    isTransferAgentSent = true;
                }
            }
            else {
                if(!isTransferAgentSent) {
                    conversation.addSystemMessageWithInput("agent not available");
                    isTransferAgentSent = true;
                }
            }
        }
        conversation.saveContextBool("hasFoundAgent", hasFoundAgent);
        conversation.saveContextBool("isTransferAgent", isTransferAgent);
        conversation.saveContextBool("isTransferAgentSent", isTransferAgentSent);
    }
    
}