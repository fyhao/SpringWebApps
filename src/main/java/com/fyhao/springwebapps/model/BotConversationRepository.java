package com.fyhao.springwebapps.model;

import java.util.UUID;

import com.fyhao.springwebapps.entity.BotConversation;

import org.springframework.data.repository.CrudRepository;

public interface BotConversationRepository extends CrudRepository<BotConversation, UUID> {
    BotConversation findByConversationId(UUID conversationId);
}
