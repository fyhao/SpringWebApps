package com.fyhao.springwebapps.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fyhao.springwebapps.dto.TwilioMessage;
import com.fyhao.springwebapps.service.TwilioService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class TwilioControllerTest {
    @Mock
    private TwilioService twilioService;

    @Test
    void acceptsTwilioWebhookAndRoutesConvertedMessage() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TwilioController(twilioService))
                .build();

        mockMvc.perform(post("/twilio/postmessage")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("MessageSid", "SM123")
                .param("From", "+6591234567")
                .param("To", "+6561234567")
                .param("Body", "Hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string("<Response/>"));

        ArgumentCaptor<TwilioMessage> twilioMessageCaptor = ArgumentCaptor.forClass(TwilioMessage.class);
        verify(twilioService).receiveMessage(twilioMessageCaptor.capture());
        TwilioMessage captured = twilioMessageCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(captured.getMessageSid()).isEqualTo("SM123");
        org.assertj.core.api.Assertions.assertThat(captured.getFrom()).isEqualTo("+6591234567");
        org.assertj.core.api.Assertions.assertThat(captured.getTo()).isEqualTo("+6561234567");
        org.assertj.core.api.Assertions.assertThat(captured.getBody()).isEqualTo("Hello");
    }

    @Test
    void rejectsWebhookWithoutFrom() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TwilioController(twilioService))
                .build();

        mockMvc.perform(post("/twilio/postmessage")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("To", "+6561234567")
                .param("Body", "Hello"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(twilioService);
    }
}
