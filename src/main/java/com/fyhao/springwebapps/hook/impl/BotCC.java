package com.fyhao.springwebapps.hook.impl;

import java.util.Date;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.ChatCustomerHook;
import com.fyhao.springwebapps.hook.HookCC;
import com.fyhao.springwebapps.service.BotService;
import com.fyhao.springwebapps.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
@HookCC
public class BotCC implements ChatCustomerHook {
    static Logger logger = LoggerFactory.getLogger(BotCC.class);

    @Autowired
    BotService botService;
    @Override
    public void preChatProcessCustomerMessage(Conversation conversation, String input) {
        
    }

    @Override
    public void postChatProcessCustomerMessage(Conversation conversation, String input) {
        String state = conversation.findContext("state");
        if(state == null || !state.equals("bot")) return;
        botService.processCustomerMessage(conversation, input);
    }
    
}