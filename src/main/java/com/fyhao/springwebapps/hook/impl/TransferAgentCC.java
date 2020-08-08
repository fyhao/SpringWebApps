package com.fyhao.springwebapps.hook.impl;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.ChatCustomerHook;
import com.fyhao.springwebapps.hook.HookCC;

import org.springframework.stereotype.Component;

@Component
@HookCC
public class TransferAgentCC implements ChatCustomerHook {

    @Override
    public void preChatProcessCustomerMessage(Conversation conversation, String input) {
        // TODO Auto-generated method stub

    }

    @Override
    public void postChatProcessCustomerMessage(Conversation conversation, String input) {
        // TODO Auto-generated method stub

    }
    
}