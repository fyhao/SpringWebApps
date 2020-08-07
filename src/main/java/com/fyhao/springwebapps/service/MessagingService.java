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
        conversation.addContext("state","bot");
        conversationRepository.save(conversation);
        return conversation.getId();
    }
    public int sendTextMessage(long conversation_id, String input) {
        Optional<Conversation> conversation = conversationRepository.findById(conversation_id);
        if(conversation.isEmpty()) {
            return 100;
        }
        Conversation conv = conversation.get();
        conv.addMessageWithInput(input);
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
}