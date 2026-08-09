package com.fyhao.springwebapps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import com.fyhao.springwebapps.dto.BotRequest;
import com.fyhao.springwebapps.dto.BotResponse;
import com.fyhao.springwebapps.entity.Bot;
import com.fyhao.springwebapps.entity.BotConversation;
import com.fyhao.springwebapps.entity.Contact;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.HookProcessor;
import com.fyhao.springwebapps.model.BotConversationRepository;
import com.fyhao.springwebapps.model.BotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {
    @Mock private HookProcessor hookProcessor;
    @Mock private BotRepository botRepository;
    @Mock private BotConversationRepository botConversationRepository;
    @Mock private ConversationalWorkflowClient workflowClient;

    private BotService botService;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        botService = new BotService(hookProcessor, botRepository, botConversationRepository, workflowClient);
        conversation = new Conversation();
        conversation.setId(UUID.randomUUID());
        conversation.setChannel("support");
        conversation.saveContext("state", "bot");
        Contact contact = new Contact();
        contact.setEmail("customer@example.com");
        conversation.setContact(contact);
    }

    @Test
    void selectsFirstConfiguredBotForChannel() {
        Bot configuredBot = bot("default", BotService.TEST_HANDLER, null);
        when(botRepository.findByChannelAndEnabledTrueOrderByPriorityAsc("support"))
                .thenReturn(Arrays.asList(configuredBot));

        botService.configureConversation(conversation, null, true);

        assertThat(conversation.findContext("botName")).isEqualTo("default");
        assertThat(conversation.findContext("botHandler")).isEqualTo("test");
        assertThat(conversation.findContext("botEnabled")).isEqualTo("true");
    }

    @Test
    void testBotCreatesInnerConversationAndResponse() {
        conversation.saveContext("botName", "test-bot");
        conversation.saveContext("botHandler", BotService.TEST_HANDLER);

        botService.processCustomerMessage(conversation, "hello");

        ArgumentCaptor<BotConversation> captor = ArgumentCaptor.forClass(BotConversation.class);
        verify(botConversationRepository).save(captor.capture());
        assertThat(captor.getValue().getConversationId()).isEqualTo(conversation.getId());
        assertThat(captor.getValue().getExternalConversationId()).isNotBlank();
        assertThat(conversation.getMessages()).hasSize(1);
        assertThat(conversation.getMessages().get(0).getContent()).isEqualTo("Test bot: hello");
    }

    @Test
    void conversationalWorkflowResponseIsConvertedToBotMessage() {
        conversation.saveContext("botName", "workflow-bot");
        conversation.saveContext("botHandler", BotService.CONVERSATIONAL_WORKFLOW_HANDLER);
        conversation.saveContext("botEndpoint", "http://workflow/messages");
        BotResponse response = new BotResponse();
        response.setConversationId("workflow-conversation-1");
        response.setContent("workflow reply");
        when(workflowClient.send(org.mockito.ArgumentMatchers.eq("http://workflow/messages"), any(BotRequest.class)))
                .thenReturn(response);

        botService.processCustomerMessage(conversation, "hello");

        ArgumentCaptor<BotConversation> captor = ArgumentCaptor.forClass(BotConversation.class);
        verify(botConversationRepository).save(captor.capture());
        assertThat(captor.getValue().getExternalConversationId()).isEqualTo("workflow-conversation-1");
        assertThat(conversation.getMessages().get(0).getContent()).isEqualTo("workflow reply");
    }

    @Test
    void disabledBotSkipsAllHandlers() {
        conversation.saveContextBool("botEnabled", false);

        botService.processCustomerMessage(conversation, "hello");

        verify(hookProcessor, never()).execute(any(Class.class), any(String.class), any());
        verify(botConversationRepository, never()).save(any(BotConversation.class));
    }

    private Bot bot(String name, String handler, String endpoint) {
        Bot bot = new Bot();
        bot.setName(name);
        bot.setChannel("support");
        bot.setHandler(handler);
        bot.setEndpoint(endpoint);
        bot.setEnabled(true);
        return bot;
    }
}
