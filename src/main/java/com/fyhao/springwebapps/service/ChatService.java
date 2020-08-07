package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.Conversation;

import org.springframework.stereotype.Service;

@Service
public class ChatService {
    
    public void processMessage(Conversation conversation, String input) {
        if(input.equals("transferagent")) {
            conversation.saveContext("state", "agent");
        }
        conversation.addMessageWithInput(input);
    }
}