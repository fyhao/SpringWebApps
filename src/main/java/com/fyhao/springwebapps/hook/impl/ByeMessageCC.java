package com.fyhao.springwebapps.hook.impl;

import java.util.Date;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.ChatCustomerHook;
import com.fyhao.springwebapps.hook.HookCC;
import com.fyhao.springwebapps.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@HookCC
public class ByeMessageCC implements ChatCustomerHook {
    static Logger logger = LoggerFactory.getLogger(ByeMessageCC.class);
    @Override
    public void preChatProcessCustomerMessage(Conversation conversation, String input) {
        if(input.equals("bye")) {
            conversation.endConversation();
        }
    }

    @Override
    public void postChatProcessCustomerMessage(Conversation conversation, String input) {
    }
    
}