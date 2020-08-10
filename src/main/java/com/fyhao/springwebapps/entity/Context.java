package com.fyhao.springwebapps.entity;

import java.io.Serializable;
import java.util.UUID;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Context implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    @Id
	@GeneratedValue
	@Column(name="id")
	UUID id;

    @Column(name="key")
    String key;

    @Column(name="value")
    String value;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    Conversation conversation;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
    
}