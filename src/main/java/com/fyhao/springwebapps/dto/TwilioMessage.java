package com.fyhao.springwebapps.dto;

/**
 * Values supplied by Twilio's incoming-message webhook.
 */
public class TwilioMessage {
    private final String messageSid;
    private final String from;
    private final String to;
    private final String body;

    public TwilioMessage(String messageSid, String from, String to, String body) {
        this.messageSid = messageSid;
        this.from = from;
        this.to = to;
        this.body = body;
    }

    public String getMessageSid() {
        return messageSid;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }
}
