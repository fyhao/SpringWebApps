package com.fyhao.springwebapps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.fyhao.springwebapps.dto.ConversationMessage;

import org.junit.jupiter.api.Test;

class MessagingServiceTest {
    @Test
    void routesInboundMessageUsingItsChannel() {
        MessagingService messagingService = spy(new MessagingService());
        ConversationMessage message = new ConversationMessage(
                "SM123", "+6591234567", "+6561234567", "Hello", "twilio");
        String conversationId = "4f8d73e7-2118-4ff5-a81a-3d499ee02974";
        doReturn(conversationId).when(messagingService).createConversation("+6591234567", "twilio");
        doReturn(0).when(messagingService).sendCustomerMessage(conversationId, "Hello");

        String result = messagingService.receiveMessage(message);

        assertThat(result).isEqualTo(conversationId);
        verify(messagingService).createConversation("+6591234567", "twilio");
        verify(messagingService).sendCustomerMessage(conversationId, "Hello");
    }
}
