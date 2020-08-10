package com.fyhao.springwebapps.hook.impl;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.ChatCustomerHook;
import com.fyhao.springwebapps.hook.HookCC;
import com.fyhao.springwebapps.service.AgentAvailabilityService;
import com.fyhao.springwebapps.service.TaskService;

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
    @Autowired
    TaskService taskService;
    @Override
    public void preChatProcessCustomerMessage(Conversation conversation, String input) {
        boolean hasFoundAgent = conversation.findContextBool("hasFoundAgent");
        boolean isTransferAgent = conversation.findContextBool("isTransferAgent");
        if(input.contains("transferagent")) {
            isTransferAgent = true;
            String agentName = agentAvailabilityService.findAgent(conversation, "hotel");
            if(agentName != null) {
                conversation.saveContext("state", "agent");
                conversation.saveContext("agentName", agentName);
                taskService.assignTask(conversation, agentName);
                hasFoundAgent = true;
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