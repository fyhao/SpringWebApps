package com.fyhao.springwebapps.ws;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import com.fyhao.springwebapps.service.MessagingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class ChannelSocketHandlerTest {
    @Mock
    private MessagingService messagingService;

    @Mock
    private WebSocketSession session;

    private ChannelSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChannelSocketHandler(messagingService);
        when(session.getAttributes()).thenReturn(new HashMap<String, Object>());
    }

    @Test
    void registersWithoutClientSuppliedServerPort() throws Exception {
        handler.handleTextMessage(session,
                new TextMessage("{\"action\":\"register\",\"conversationid\":\"conversation1\"}"));

        verify(messagingService).checkExistingMessageForCustomer("conversation1");
        org.assertj.core.api.Assertions.assertThat(session.getAttributes())
                .containsEntry("conversationid", "conversation1")
                .doesNotContainKey("serverport");
    }

    @Test
    void routesCustomerActionsDirectlyToMessagingService() throws Exception {
        handler.handleTextMessage(session, new TextMessage(
                "{\"action\":\"sendChatMessage\",\"conversationid\":\"conversation1\",\"chatMessage\":\"hello\"}"));
        handler.handleTextMessage(session, new TextMessage(
                "{\"action\":\"startTyping\",\"conversationid\":\"conversation1\"}"));
        handler.handleTextMessage(session, new TextMessage(
                "{\"action\":\"stopTyping\",\"conversationid\":\"conversation1\"}"));

        verify(messagingService).sendCustomerMessage("conversation1", "hello");
        verify(messagingService).sendCustomerStartTyping("conversation1");
        verify(messagingService).sendCustomerStopTyping("conversation1");
    }
}
