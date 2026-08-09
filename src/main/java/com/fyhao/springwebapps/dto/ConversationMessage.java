package com.fyhao.springwebapps.dto;

/**
 * Channel-neutral representation of an inbound customer message.
 */
public class ConversationMessage {
    private final String externalId;
    private final String from;
    private final String to;
    private final String content;
    private final String channel;

    public ConversationMessage(String externalId, String from, String to, String content, String channel) {
        this.externalId = externalId;
        this.from = from;
        this.to = to;
        this.content = content;
        this.channel = channel;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }

    public String getChannel() {
        return channel;
    }
}
