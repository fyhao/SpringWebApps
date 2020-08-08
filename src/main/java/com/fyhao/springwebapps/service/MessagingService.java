package com.fyhao.springwebapps.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.fyhao.springwebapps.entity.Contact;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Message;
import com.fyhao.springwebapps.model.ContactRepository;
import com.fyhao.springwebapps.model.ConversationRepository;
import com.fyhao.springwebapps.model.MessageRepository;
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
        return 0;
    }
    public int sendAgentMessage(String conversation_id, String agentName, String input) {
        Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversation_id));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        chatService.processAgentMessage(conversation, agentName, input);
        conversationRepository.save(conversation);
        ChannelSocketHandler.sendChatMessageToCustomer(conversation.getId().toString(), input);
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