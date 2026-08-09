package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class BotConversation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private UUID id;

    private UUID conversationId;
    private String botName;
    private String externalConversationId;
    private Timestamp createdTime;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public String getBotName() { return botName; }
    public void setBotName(String botName) { this.botName = botName; }
    public String getExternalConversationId() { return externalConversationId; }
    public void setExternalConversationId(String externalConversationId) { this.externalConversationId = externalConversationId; }
    public Timestamp getCreatedTime() { return createdTime; }
    public void setCreatedTime(Timestamp createdTime) { this.createdTime = createdTime; }
}
