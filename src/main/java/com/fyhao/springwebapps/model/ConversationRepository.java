package com.fyhao.springwebapps.model;

import java.util.UUID;

import com.fyhao.springwebapps.entity.Conversation;

import org.springframework.data.repository.CrudRepository;

public interface ConversationRepository extends CrudRepository<Conversation, UUID> {
    
}