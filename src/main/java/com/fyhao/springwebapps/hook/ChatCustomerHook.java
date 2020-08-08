package com.fyhao.springwebapps.hook;

import com.fyhao.springwebapps.entity.Conversation;

public interface ChatCustomerHook {
    public void preChatProcessCustomerMessage(Conversation conversation, String input);
    public void postChatProcessCustomerMessage(Conversation conversation, String input);
}