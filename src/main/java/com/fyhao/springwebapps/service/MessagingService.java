package com.fyhao.springwebapps.service;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.fyhao.springwebapps.entity.Contact;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Message;
import com.fyhao.springwebapps.entity.Task;
import com.fyhao.springwebapps.model.ContactRepository;
import com.fyhao.springwebapps.model.ConversationRepository;
import com.fyhao.springwebapps.model.MessageRepository;
import com.fyhao.springwebapps.ws.AgentSocketHandler;
import com.fyhao.springwebapps.ws.ChannelSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;

@Service
public class MessagingService {
    static Logger logger = LoggerFactory.getLogger(MessagingService.class);
    @Autowired
    ConversationRepository conversationRepository;
    
    @Autowired
    MessageRepository messageRepository;
    
    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ChatService chatService;

    public String createConversation(String email) {
        return createConversation(email, "webchat");
    }
    public String createConversation(String email, String channel) {
        Contact contact = contactRepository.findByEmail(email);
        if(contact == null) {
            contact = new Contact();
            contact.setEmail(email);
            contact.setCreatedTime(new Timestamp(new Date().getTime()));
            contact.setName(email);
            contactRepository.save(contact);
        }
        Conversation conversation = new Conversation();
        conversation.setContact(contact);
        conversation.setStartTime(new Timestamp(new Date().getTime()));
        conversation.setChannel(channel);
        conversation.saveContext("state","bot");
        conversation.addActivity("conversationStarted");
        conversationRepository.save(conversation);
        sendSystemMessage(conversation.getId().toString(), "Hi welcome " + contact.getEmail());
        return conversation.getId().toString();
    }
    public int sendSystemMessage(String conversation_id, String input) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        chatService.processSystemMessage(conversation, input);
        conversationRepository.save(conversation);
        return 0;
    }
    public int sendCustomerMessage(String conversation_id, String input) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        chatService.processCustomerMessage(conversation, input);
        conversationRepository.save(conversation);
        String state = conversation.findContext("state");
        logger.info("MessagingService sendCustomerMessage state " + conversation_id + " " + input + " " + state);
        if(state.equals("agent")) {
        	Task task = conversation.getTask();
            String activeAgentsStr = conversation.findContext("activeAgents");
            if(activeAgentsStr != null) {
            	String[] activeAgents = activeAgentsStr.split("\\,");
            	for(String agentName : activeAgents) {
                	logger.info("MessagingService sendCustomerMessage to send agent " + agentName + " for " + input);
                    AgentSocketHandler.sendCustomerMessage(conversation.getId().toString(), task.getId().toString(), agentName, input);
                }
            }
            String bargeinAgentsStr = conversation.findContext("bargeinAgents");
            if(bargeinAgentsStr != null) {
            	String[] bargeinAgents = bargeinAgentsStr.split("\\,");
            	for(String agentName : bargeinAgents) {
                	logger.info("MessagingService sendCustomerMessage to send agent " + agentName + " for " + input);
                    AgentSocketHandler.sendCustomerMessage(conversation.getId().toString(), task.getId().toString(), agentName, input);
                }
            }
        }
        return 0;
    }
    public int sendAgentMessage(String conversation_id, String agentName, String input) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        String activeAgentsStr = conversation.findContext("activeAgents");
        if(activeAgentsStr == null) return 101;
        if(!Arrays.asList(activeAgentsStr.split("\\,")).contains(agentName)) {
        	return 102;
        }
        chatService.processAgentMessage(conversation, agentName, input);
        conversationRepository.save(conversation);
        ChannelSocketHandler.sendChatMessageToCustomer(conversation.getId().toString(), input);
        Task task = conversation.getTask();
        String[] activeAgents = activeAgentsStr.split("\\,");
        for(String activeAgent : activeAgents) {
        	if(!activeAgent.equals(agentName)) {
        		AgentSocketHandler.sendCustomerMessage(conversation.getId().toString(), task.getId().toString(), activeAgent, input);
        	}
        }
        String bargeinAgentsStr = conversation.findContext("bargeinAgents");
        if(bargeinAgentsStr != null) {
        	String[] bargeinAgents = bargeinAgentsStr.split("\\,");
        	for(String bargeinAgent : bargeinAgents) {
        		AgentSocketHandler.sendCustomerMessage(conversation.getId().toString(), task.getId().toString(), bargeinAgent, input);
        	}
        }
        return 0;
    }
    public int sendBotMessage(String conversation_id, String input) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        chatService.processBotMessage(conversation, input);
        conversationRepository.save(conversation);
        ChannelSocketHandler.sendChatMessageToCustomer(conversation.getId().toString(), input);
        return 0;
    }
    public int sendCustomerStartTyping(String conversation_id) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        String agentid = conversation.getTask().getAgent().getName();
        AgentSocketHandler.sendCustomerStartedTypingEvent(agentid, conversation_id);
        return 0;
    }
    public int sendCustomerStopTyping(String conversation_id) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        String agentid = conversation.getTask().getAgent().getName();
        AgentSocketHandler.sendCustomerStoppedTypingEvent(agentid, conversation_id);
        return 0;
    }
    public int getMessageCount(String conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return -1;
        }
        Conversation conv = conversation.get();
        return conv.getMessages().size();
    }
    public String findContext(String conversation_id, String key) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        return conv.findContext(key);
    }
    public String findChannel(String conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        return conv.getChannel();
    }
    public long getContactsCount() {
        return contactRepository.count();
    }
    public String getConversationEndTime(String conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        if(conv.getEndTime() != null) return conv.getEndTime().toString();
        return null;
    }
    public String getLastMessageFromParty(String conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        Message message = conv.getMessages().get(conv.getMessages().size() - 1);
        return message.getFromparty();
    }
    public String getLastMessageToParty(String conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        Message message = conv.getMessages().get(conv.getMessages().size() - 1);
        return message.getToparty();
    }
    public String getLastMessageContent(String conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        Message message = conv.getMessages().get(conv.getMessages().size() - 1);
        return message.getContent();
    }
}