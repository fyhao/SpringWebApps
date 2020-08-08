package com.fyhao.springwebapps.service;

import java.util.Date;
import java.util.Optional;

import com.fyhao.springwebapps.entity.Contact;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.model.ContactRepository;
import com.fyhao.springwebapps.model.ConversationRepository;
import com.fyhao.springwebapps.model.MessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;

@Service
public class MessagingService {
    
    @Autowired
    ConversationRepository conversationRepository;
    
    @Autowired
    MessageRepository messageRepository;
    
    @Autowired
    ContactRepository contactRepository;

    @Autowired
    ChatService chatService;

    public long createConversation(String email) {
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
        conversation.setChannel("webchat");
        conversation.setEndTime(new Timestamp(new Date().getTime()));
        conversation.saveContext("state","bot");
        conversationRepository.save(conversation);
        return conversation.getId();
    }
    public int sendTextMessage(long conversation_id, String input) {
        Optional<Conversation> conv = conversationRepository.findById(conversation_id);
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        chatService.processMessage(conversation, input);
        conversationRepository.save(conversation);
        return 0;
    }
    public int getMessageCount(long conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(conversation_id);
        if(conversation.isEmpty()) {
            return -1;
        }
        Conversation conv = conversation.get();
        return conv.getMessages().size();
    }
    public String findContext(long conversation_id, String key) {
        Optional<Conversation> conversation = conversationRepository.findById(conversation_id);
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        return conv.findContext(key);
    }
    public String findChannel(long conversation_id) {
        Optional<Conversation> conversation = conversationRepository.findById(conversation_id);
        if(conversation.isEmpty()) {
            return null;
        }
        Conversation conv = conversation.get();
        return conv.getChannel();
    }
    public long getContactsCount() {
        return contactRepository.count();
    }
}