package com.fyhao.springwebapps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fyhao.springwebapps.dto.ConversationMessage;
import com.fyhao.springwebapps.dto.TwilioMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TwilioServiceTest {
    @Mock
    private MessagingService messagingService;

    @Test
    void convertsTwilioMessageToConversationMessage() {
        TwilioService twilioService = new TwilioService(messagingService);
        TwilioMessage twilioMessage = new TwilioMessage(
                "SM123", "whatsapp:+6591234567", "whatsapp:+6561234567", "Hello");

        ConversationMessage result = twilioService.parseMessage(twilioMessage);

        assertThat(result.getExternalId()).isEqualTo("SM123");
        assertThat(result.getFrom()).isEqualTo("whatsapp:+6591234567");
        assertThat(result.getTo()).isEqualTo("whatsapp:+6561234567");
        assertThat(result.getContent()).isEqualTo("Hello");
        assertThat(result.getChannel()).isEqualTo("twilio");
    }

    @Test
    void rejectsMessageWithoutSender() {
        TwilioService twilioService = new TwilioService(messagingService);
        TwilioMessage twilioMessage = new TwilioMessage("SM123", " ", "+6561234567", "Hello");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> twilioService.parseMessage(twilioMessage))
                .withMessage("Twilio From is required");
    }

    @Test
    void routesConvertedMessageThroughMessagingService() {
        TwilioService twilioService = new TwilioService(messagingService);
        TwilioMessage twilioMessage = new TwilioMessage(
                "SM123", "+6591234567", "+6561234567", "Hello");
        when(messagingService.receiveMessage(org.mockito.ArgumentMatchers.any(ConversationMessage.class)))
                .thenReturn("conversation-123");

        String result = twilioService.receiveMessage(twilioMessage);

        org.mockito.ArgumentCaptor<ConversationMessage> captor =
                org.mockito.ArgumentCaptor.forClass(ConversationMessage.class);
        verify(messagingService).receiveMessage(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("twilio");
        assertThat(captor.getValue().getContent()).isEqualTo("Hello");
        assertThat(result).isEqualTo("conversation-123");
    }
}
