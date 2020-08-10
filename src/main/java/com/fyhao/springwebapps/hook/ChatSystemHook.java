package com.fyhao.springwebapps.hook;

import com.fyhao.springwebapps.entity.Conversation;

public interface ChatSystemHook {
    public void preChatProcessSystemMessage(Conversation conversation, String input);
    public void postChatProcessSystemMessage(Conversation conversation, String input);
}