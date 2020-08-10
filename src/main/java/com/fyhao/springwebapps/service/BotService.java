package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.HookBS;
import com.fyhao.springwebapps.hook.HookProcessor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    
    
    @Autowired
    HookProcessor hookProcessor;
    
    public void processCustomerMessage(Conversation conversation, String input) {
        hookProcessor.execute(HookBS.class, "processCustomerMessage", conversation, input);
    }
    
}