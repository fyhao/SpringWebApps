package com.fyhao.springwebapps.dto;

public class BotResponse {
    private String conversationId;
    private String content;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
