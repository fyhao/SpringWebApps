package com.fyhao.springwebapps.dto;

public class BotRequest {
    private String conversationId;
    private String botConversationId;
    private String channel;
    private String input;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getBotConversationId() { return botConversationId; }
    public void setBotConversationId(String botConversationId) { this.botConversationId = botConversationId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}
