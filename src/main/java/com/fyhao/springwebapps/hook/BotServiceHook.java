package com.fyhao.springwebapps.hook;

import com.fyhao.springwebapps.entity.Conversation;

public interface BotServiceHook {
    public void processCustomerMessage(Conversation conversation, String input);
}