package com.fyhao.springwebapps.service;

import java.util.Date;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.util.Util;

import org.springframework.stereotype.Service;

@Service
public class ChatService {
    
    public void processCustomerMessage(Conversation conversation, String input) {
        if(input.equals("transferagent")) {
            conversation.saveContext("state", "agent");
        }
        else if(input.equals("bye")) {
            conversation.saveContext("state", "end");
            conversation.setEndTime(Util.getSQLTimestamp(new Date()));
        }
        conversation.addMessageWithInput(input);
    }
    public void processSystemMessage(Conversation conversation, String input) {
        conversation.addSystemMessageWithInput(input);
    }
}