package com.fyhao.springwebapps.model;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.fyhao.springwebapps.entity.ConversationActivity;

public interface ConversationActivityRepository extends CrudRepository<ConversationActivity, UUID> {
    
}
