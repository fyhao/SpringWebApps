package com.fyhao.springwebapps.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fyhao.springwebapps.dto.BotRequest;
import com.fyhao.springwebapps.dto.BotResponse;
import com.fyhao.springwebapps.entity.Bot;
import com.fyhao.springwebapps.entity.BotConversation;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.HookBS;
import com.fyhao.springwebapps.hook.HookProcessor;
import com.fyhao.springwebapps.model.BotConversationRepository;
import com.fyhao.springwebapps.model.BotRepository;
import com.fyhao.springwebapps.ws.ChannelSocketHandler;

import org.springframework.stereotype.Service;

@Service
public class BotService {
    public static final String TEST_HANDLER = "test";
    public static final String CONVERSATIONAL_WORKFLOW_HANDLER = "conversational-wf";

    private final HookProcessor hookProcessor;
    private final BotRepository botRepository;
    private final BotConversationRepository botConversationRepository;
    private final ConversationalWorkflowClient workflowClient;

    public BotService(HookProcessor hookProcessor, BotRepository botRepository,
            BotConversationRepository botConversationRepository,
            ConversationalWorkflowClient workflowClient) {
        this.hookProcessor = hookProcessor;
        this.botRepository = botRepository;
        this.botConversationRepository = botConversationRepository;
        this.workflowClient = workflowClient;
    }

    public void configureConversation(Conversation conversation, String requestedBotName, boolean enabled) {
        conversation.saveContextBool("botEnabled", enabled);
        if (!enabled) {
            return;
        }

        Bot bot = null;
        if (requestedBotName != null && !requestedBotName.trim().isEmpty()) {
            bot = botRepository.findFirstByChannelAndNameAndEnabledTrue(
                    conversation.getChannel(), requestedBotName);
        } else {
            List<Bot> bots = botRepository.findByChannelAndEnabledTrueOrderByPriorityAsc(conversation.getChannel());
            if (bots != null && !bots.isEmpty()) {
                bot = bots.get(0);
            }
        }
        if (bot != null) {
            conversation.saveContext("botName", bot.getName());
            conversation.saveContext("botHandler", bot.getHandler());
            conversation.saveContext("botEndpoint", bot.getEndpoint());
        }
    }
    
    public void processCustomerMessage(Conversation conversation, String input) {
        if ("false".equals(conversation.findContext("botEnabled"))) {
            return;
        }
        String handler = conversation.findContext("botHandler");
        if (TEST_HANDLER.equals(handler)) {
            sendConfiguredBotMessage(conversation, input, false);
            return;
        }
        if (CONVERSATIONAL_WORKFLOW_HANDLER.equals(handler)) {
            sendConfiguredBotMessage(conversation, input, true);
            return;
        }
        hookProcessor.execute(HookBS.class, "processCustomerMessage", conversation, input);
    }

    private void sendConfiguredBotMessage(Conversation conversation, String input, boolean remote) {
        BotConversation botConversation = botConversationRepository.findByConversationId(conversation.getId());
        if (botConversation == null) {
            botConversation = new BotConversation();
            botConversation.setConversationId(conversation.getId());
            botConversation.setBotName(conversation.findContext("botName"));
            botConversation.setExternalConversationId(UUID.randomUUID().toString());
            botConversation.setCreatedTime(new Timestamp(new Date().getTime()));
        }

        String content;
        if (remote) {
            BotRequest request = new BotRequest();
            request.setConversationId(conversation.getId().toString());
            request.setBotConversationId(botConversation.getExternalConversationId());
            request.setChannel(conversation.getChannel());
            request.setInput(input);
            BotResponse response = workflowClient.send(conversation.findContext("botEndpoint"), request);
            if (response == null || response.getContent() == null) {
                return;
            }
            if (response.getConversationId() != null) {
                botConversation.setExternalConversationId(response.getConversationId());
            }
            content = response.getContent();
        } else {
            content = "Test bot: " + input;
        }

        botConversationRepository.save(botConversation);
        conversation.addBotMessageWithInput(content);
        ChannelSocketHandler.sendChatMessageToCustomer(conversation.getId().toString(), content);
    }
}
