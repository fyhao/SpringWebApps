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
    //com.fyhao.springwebapps.hook.impl.ByeMessageCC.preChatProcessCustomerMessage()
    @Override
    public void preChatProcessCustomerMessage(Conversation conversation, String input) {
        logger.info("ByeMessageCC preChatProcessCustomerMessage");
        // TODO Auto-generated method stub
        if(input.equals("bye")) {
            conversation.saveContext("state", "end");
            conversation.setEndTime(Util.getSQLTimestamp(new Date()));
        }
    }

    @Override
    public void postChatProcessCustomerMessage(Conversation conversation, String input) {
        // TODO Auto-generated method stub
        logger.info("ByeMessageCC postChatProcessCustomerMessage");
    }
    
}