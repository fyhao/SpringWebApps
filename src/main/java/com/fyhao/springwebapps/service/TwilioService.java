package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.dto.ConversationMessage;
import com.fyhao.springwebapps.dto.TwilioMessage;

import org.springframework.stereotype.Service;

@Service
public class TwilioService {
    public static final String CHANNEL = "twilio";
    private final MessagingService messagingService;

    public TwilioService(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public String receiveMessage(TwilioMessage message) {
        return messagingService.receiveMessage(parseMessage(message));
    }

    public ConversationMessage parseMessage(TwilioMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Twilio message is required");
        }
        if (isBlank(message.getFrom())) {
            throw new IllegalArgumentException("Twilio From is required");
        }
        if (message.getBody() == null) {
            throw new IllegalArgumentException("Twilio Body is required");
        }

        return new ConversationMessage(
                message.getMessageSid(),
                message.getFrom(),
                message.getTo(),
                message.getBody(),
                CHANNEL);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
